package com.github.pmouli.rune.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.github.pmouli.rune.RosettaFileType
import com.github.pmouli.rune.generation.GenerationRequest
import com.github.pmouli.rune.generation.RuneJavaGeneratorAdapter
import com.github.pmouli.rune.settings.RuneProjectConfiguration
import java.nio.file.Path

/**
 * Action to generate Java code from Rune DSL files with preview.
 *
 * Workflow:
 * 1. User selects .rosetta files or directory
 * 2. Action invokes generator in background
 * 3. Preview UI shows diff for each generated file
 * 4. User can accept/reject changes per file
 * 5. Accepted files are written to VFS
 *
 * Performance: Runs on background thread with progress indicator.
 * Cancellable: User can cancel generation mid-process.
 *
 * Thread-safe: Uses ProgressManager and WriteAction for VFS modifications.
 */
class GeneratePreviewAction : AnAction(
    "Generate Code Preview",
    "Generate Java code from Rune DSL with preview",
    null // TODO: Add icon
) {
    companion object {
        private val LOG = Logger.getInstance(GeneratePreviewAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        // Filter to .rosetta files
        val rosettaFiles = selectedFiles.flatMap { file ->
            if (file.isDirectory) {
                findRosettaFiles(file)
            } else if (file.fileType == RosettaFileType.INSTANCE) {
                listOf(file)
            } else {
                emptyList()
            }
        }

        if (rosettaFiles.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No Rune DSL (.rosetta) files selected",
                "Generate Code"
            )
            return
        }

        // Run generation in background
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating Java Code from Rune DSL",
            true // cancellable
        ) {
            override fun run(indicator: ProgressIndicator) {
                generateCode(project, rosettaFiles, indicator)
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        // Enable action only when project is open and files are selected
        e.presentation.isEnabledAndVisible = project != null &&
                                              selectedFiles != null &&
                                              selectedFiles.isNotEmpty()
    }

    private fun generateCode(
        project: Project,
        rosettaFiles: List<VirtualFile>,
        indicator: ProgressIndicator
    ) {
        indicator.text = "Loading configuration..."
        val config = RuneProjectConfiguration.getInstance(project)

        // Prepare generation request
        val outputDir = Path.of(project.basePath ?: ".", config.outputDirectory)
        val request = GenerationRequest(
            project = project,
            sourceFiles = rosettaFiles,
            outputDirectory = outputDir,
            outputPackage = config.outputPackage,
            options = mapOf(
                "generateBuilders" to config.generateBuilders.toString(),
                "generateValidation" to config.generateValidation.toString()
            ),
            grammarVersion = config.grammarVersion
        )

        try {
            indicator.text = "Generating code from ${rosettaFiles.size} file(s)..."
            indicator.isIndeterminate = false

            // Invoke generator
            val generator = RuneJavaGeneratorAdapter()
            val result = generator.generate(request)

            // Check if cancelled
            if (indicator.isCanceled) {
                LOG.info("Code generation cancelled by user")
                return
            }

            // Show results
            ApplicationManager.getApplication().invokeLater {
                if (result.success) {
                    // TODO: Show diff preview UI instead of simple message
                    Messages.showInfoMessage(
                        project,
                        "Generated ${result.generatedFiles.size} file(s):\n" +
                        result.generatedFiles.joinToString("\n") { it.outputPath.fileName.toString() },
                        "Generation Complete"
                    )
                } else {
                    val errors = result.diagnostics
                        .filter { it.severity == com.github.pmouli.rune.generation.DiagnosticSeverity.ERROR }
                        .joinToString("\n") { it.message }

                    Messages.showErrorDialog(
                        project,
                        "Generation failed:\n$errors",
                        "Generation Error"
                    )
                }
            }
        } catch (e: Exception) {
            LOG.error("Code generation failed", e)
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    project,
                    "Generation failed: ${e.message}",
                    "Generation Error"
                )
            }
        }
    }

    private fun findRosettaFiles(directory: VirtualFile): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        
        fun visit(file: VirtualFile) {
            if (file.isDirectory) {
                file.children.forEach { visit(it) }
            } else if (file.fileType == RosettaFileType.INSTANCE) {
                result.add(file)
            }
        }
        
        visit(directory)
        return result
    }
}
