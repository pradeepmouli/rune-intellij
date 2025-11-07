package dev.mouli.rune.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.nio.file.Path

/**
 * Background task for compiling Rune DSL sources and generating code.
 *
 * Runs as a cancellable background task with progress reporting.
 * Uses the configured generator adapter to process .rosetta files.
 *
 * Thread-safe: Execution happens on background thread with read/write actions.
 */
class RosettaGenerationCompileTask(
    project: Project,
    private val sourceFiles: List<VirtualFile>,
    private val outputDirectory: Path,
    private val outputPackage: String,
    private val generatorOptions: Map<String, String> = emptyMap(),
    private val onComplete: (GenerationResult) -> Unit,
) : Task.Backgroundable(project, "Generating Code from Rune DSL", true) {
    companion object {
        private val LOG = Logger.getInstance(RosettaGenerationCompileTask::class.java)
    }

    override fun run(indicator: ProgressIndicator) {
        indicator.text = "Preparing code generation..."
        indicator.isIndeterminate = false

        try {
            // Validate input
            if (sourceFiles.isEmpty()) {
                val result =
                    GenerationResult(
                        success = false,
                        generatedFiles = emptyList(),
                        diagnostics =
                            listOf(
                                GeneratorDiagnostic(
                                    severity = DiagnosticSeverity.ERROR,
                                    message = "No Rune DSL source files selected for generation",
                                    sourceFile = null,
                                    line = null,
                                    column = null,
                                ),
                            ),
                    )
                onComplete(result)
                return
            }

            indicator.text = "Compiling ${sourceFiles.size} Rune DSL file(s)..."
            indicator.fraction = 0.1

            // Get generator adapter (for now, using the Java adapter)
            val generator = RuneJavaGeneratorAdapter()
            LOG.info("Using generator: ${generator.metadata.displayName} v${generator.metadata.version}")

            indicator.text = "Generating Java sources..."
            indicator.fraction = 0.3

            // Create generation request
            val request =
                GenerationRequest(
                    project = project,
                    sourceFiles = sourceFiles,
                    outputDirectory = outputDirectory,
                    outputPackage = outputPackage,
                    options = generatorOptions,
                )

            // Check for cancellation
            if (indicator.isCanceled) {
                LOG.info("Code generation cancelled by user")
                return
            }

            // Execute generation
            val result = generator.generate(request)
            indicator.fraction = 0.8

            indicator.text = "Refreshing file system..."
            // Refresh VFS to make generated files visible in IDE
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
            indicator.fraction = 0.9

            // Log results
            if (result.success) {
                LOG.info("Code generation completed successfully: ${result.generatedFiles.size} files generated")
            } else {
                LOG.warn(
                    "Code generation completed with errors: ${result.diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }.size} errors",
                )
            }

            indicator.fraction = 1.0
            indicator.text = "Code generation complete"

            // Invoke completion callback
            onComplete(result)
        } catch (e: Exception) {
            LOG.error("Code generation task failed", e)
            val errorResult =
                GenerationResult(
                    success = false,
                    generatedFiles = emptyList(),
                    diagnostics =
                        listOf(
                            GeneratorDiagnostic(
                                severity = DiagnosticSeverity.ERROR,
                                message = "Generation task failed: ${e.message}",
                                sourceFile = null,
                                line = null,
                                column = null,
                            ),
                        ),
                )
            onComplete(errorResult)
        }
    }

    override fun onCancel() {
        LOG.info("Code generation task cancelled")
        // Cleanup can be added here if needed
    }

    override fun onThrowable(error: Throwable) {
        LOG.error("Unexpected error during code generation", error)
        val errorResult =
            GenerationResult(
                success = false,
                generatedFiles = emptyList(),
                diagnostics =
                    listOf(
                        GeneratorDiagnostic(
                            severity = DiagnosticSeverity.ERROR,
                            message = "Unexpected error: ${error.message}",
                            sourceFile = null,
                            line = null,
                            column = null,
                        ),
                    ),
            )
        onComplete(errorResult)
    }
}
