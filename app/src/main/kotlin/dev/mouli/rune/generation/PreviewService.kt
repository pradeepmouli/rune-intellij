package dev.mouli.rune.generation

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.nio.file.Files

/**
 * Service for previewing and applying code generation results.
 *
 * Provides diff preview using IntelliJ's DiffManager and handles
 * applying generated files to the project with user confirmation.
 *
 * Thread-safe: Service methods use read/write actions appropriately.
 */
@Service(Service.Level.PROJECT)
class PreviewService(private val project: Project) {
    companion object {
        private val LOG = Logger.getInstance(PreviewService::class.java)

        fun getInstance(project: Project): PreviewService {
            return project.getService(PreviewService::class.java)
        }
    }

    /**
     * Show diff preview for generated files.
     *
     * Opens a diff viewer comparing existing files (if any) with generated content.
     * User can review changes before applying them.
     *
     * @param result Generation result containing generated files
     * @param onApply Callback to invoke if user chooses to apply changes
     */
    fun showPreview(
        result: GenerationResult,
        onApply: () -> Unit,
    ) {
        if (result.generatedFiles.isEmpty()) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showInfoMessage(
                    project,
                    "No files were generated.",
                    "Code Generation",
                )
            }
            return
        }

        // Show summary dialog with option to preview and apply
        ApplicationManager.getApplication().invokeLater {
            val message =
                buildString {
                    append("Generation completed:\n")
                    append("  ${result.generatedFiles.size} file(s) generated\n")

                    val errors = result.diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
                    val warnings = result.diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

                    if (errors > 0) append("  $errors error(s)\n")
                    if (warnings > 0) append("  $warnings warning(s)\n")

                    append("\nWould you like to preview the changes?")
                }

            val response =
                Messages.showYesNoDialog(
                    project,
                    message,
                    "Code Generation Complete",
                    "Preview Changes",
                    "Apply Without Preview",
                    Messages.getQuestionIcon(),
                )

            when (response) {
                Messages.YES -> showDiffPreview(result, onApply)
                Messages.NO -> onApply()
            }
        }
    }

    /**
     * Show diff viewer for each generated file.
     *
     * Displays side-by-side comparison of existing vs. generated content.
     */
    private fun showDiffPreview(
        result: GenerationResult,
        onApply: () -> Unit,
    ) {
        if (result.generatedFiles.isEmpty()) return

        // For now, show diff for the first generated file
        // TODO: Implement multi-file diff navigation
        val firstFile = result.generatedFiles.first()

        ReadAction.run<RuntimeException> {
            showDiffForFile(firstFile, onApply)
        }
    }

    /**
     * Show diff for a single generated file.
     */
    private fun showDiffForFile(
        generatedFile: GeneratedFile,
        onApply: () -> Unit,
    ) {
        val outputPath = generatedFile.outputPath

        try {
            // Read generated content
            val generatedContent = Files.readString(outputPath)

            // Find existing file if it exists
            val vfsPath = "file://${outputPath.toAbsolutePath()}"
            val existingFile = VirtualFileManager.getInstance().findFileByUrl(vfsPath)

            val existingContent =
                existingFile?.let { file ->
                    FileDocumentManager.getInstance().getDocument(file)?.text ?: ""
                } ?: ""

            // Create diff contents
            val contentFactory = DiffContentFactory.getInstance()
            val leftContent = contentFactory.create(project, existingContent, existingFile?.fileType)
            val rightContent = contentFactory.create(project, generatedContent)

            // Create diff request
            val diffRequest =
                SimpleDiffRequest(
                    "Generated: ${outputPath.fileName}",
                    leftContent,
                    rightContent,
                    if (existingFile != null) "Current" else "Empty",
                    "Generated",
                )

            // Show diff in dialog
            ApplicationManager.getApplication().invokeLater {
                DiffManager.getInstance().showDiff(project, diffRequest)

                // After showing diff, ask if user wants to apply
                val apply =
                    Messages.showYesNoDialog(
                        project,
                        "Apply generated changes?",
                        "Code Generation",
                        Messages.getQuestionIcon(),
                    )

                if (apply == Messages.YES) {
                    onApply()
                }
            }
        } catch (e: Exception) {
            LOG.error("Failed to show diff preview", e)
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    project,
                    "Failed to show preview: ${e.message}",
                    "Preview Error",
                )
            }
        }
    }

    /**
     * Apply generated files to the project.
     *
     * Copies generated files to the project and refreshes VFS.
     *
     * @param result Generation result containing files to apply
     * @param targetDirectory Target directory in project VFS
     */
    fun applyGeneratedFiles(
        result: GenerationResult,
        targetDirectory: VirtualFile,
    ) {
        ApplicationManager.getApplication().runWriteAction {
            try {
                var appliedCount = 0

                result.generatedFiles.forEach { generatedFile ->
                    try {
                        val content = Files.readString(generatedFile.outputPath)
                        val fileName = generatedFile.outputPath.fileName.toString()

                        // Create or update file in project
                        val existingFile = targetDirectory.findChild(fileName)
                        val file =
                            if (existingFile != null) {
                                // Update existing file
                                val document = FileDocumentManager.getInstance().getDocument(existingFile)
                                document?.setText(content)
                                existingFile
                            } else {
                                // Create new file
                                val newFile = targetDirectory.createChildData(this, fileName)
                                newFile.setBinaryContent(content.toByteArray())
                                newFile
                            }

                        appliedCount++
                        LOG.info("Applied generated file: ${file.path}")
                    } catch (e: Exception) {
                        LOG.error("Failed to apply generated file: ${generatedFile.outputPath}", e)
                    }
                }

                // Refresh VFS
                VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)

                ApplicationManager.getApplication().invokeLater {
                    Messages.showInfoMessage(
                        project,
                        "Successfully applied $appliedCount generated file(s)",
                        "Code Generation",
                    )
                }
            } catch (e: Exception) {
                LOG.error("Failed to apply generated files", e)
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Failed to apply generated files: ${e.message}",
                        "Generation Error",
                    )
                }
            }
        }
    }
}
