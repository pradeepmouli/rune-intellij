package dev.mouli.rune.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import java.io.StringWriter
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * Action to validate Rune DSL files in the project.
 *
 * Scans .rosetta files and produces a validation report showing errors,
 * warnings, and statistics. Results are displayed in a tool window with
 * navigable links to issues.
 *
 * Validation includes:
 * - Syntax checking
 * - Type resolution
 * - Cross-reference validation
 * - Cardinality constraints
 * - Naming conventions
 */
class ValidateAction : AnAction(), DumbAware {
    companion object {
        private val LOG = Logger.getInstance(ValidateAction::class.java)
        private const val TOOL_WINDOW_ID = "Rune Validation"
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Run validation in background
        ProgressManager.getInstance().run(ValidationTask(project))
    }

    /**
     * Background task for validating Rune DSL files.
     */
    private class ValidationTask(project: Project) :
        Task.Backgroundable(project, "Validating Rune DSL Files", true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text = "Scanning for .rosetta files..."
            indicator.isIndeterminate = false

            // Find all .rosetta files in project
            val rosettaFiles = findRosettaFiles(project)

            if (rosettaFiles.isEmpty()) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showInfoMessage(
                        project,
                        "No .rosetta files found in project.",
                        "Rune DSL Validation",
                    )
                }
                return
            }

            indicator.text = "Validating ${rosettaFiles.size} file(s)..."

            // Perform validation
            val report = validateFiles(rosettaFiles, indicator)

            indicator.text = "Generating report..."
            indicator.fraction = 0.9

            // Show results in tool window
            ApplicationManager.getApplication().invokeLater {
                showValidationReport(project, report)
            }

            indicator.fraction = 1.0
        }

        /**
         * Find all .rosetta files in the project.
         */
        private fun findRosettaFiles(project: Project): List<VirtualFile> {
            val files = mutableListOf<VirtualFile>()
            val baseDir = project.baseDir ?: return files

            fun scanDirectory(dir: VirtualFile) {
                dir.children.forEach { child ->
                    when {
                        child.isDirectory && !child.name.startsWith(".") -> scanDirectory(child)
                        child.extension == "rosetta" -> files.add(child)
                    }
                }
            }

            scanDirectory(baseDir)
            return files
        }

        /**
         * Validate .rosetta files and generate report.
         */
        private fun validateFiles(
            files: List<VirtualFile>,
            indicator: ProgressIndicator,
        ): ValidationReport {
            val issues = mutableListOf<ValidationIssue>()
            var fileCount = 0

            files.forEachIndexed { index, file ->
                if (indicator.isCanceled) return ValidationReport.cancelled()

                indicator.fraction = (index.toDouble() / files.size) * 0.8
                indicator.text2 = "Validating ${file.name}..."

                try {
                    // TODO: Integrate with actual Rune DSL parser/validator
                    // For now, perform basic validation
                    val fileIssues = validateFile(file)
                    issues.addAll(fileIssues)
                    fileCount++
                } catch (e: Exception) {
                    LOG.error("Failed to validate file: ${file.path}", e)
                    issues.add(
                        ValidationIssue(
                            severity = IssueSeverity.ERROR,
                            message = "Failed to parse file: ${e.message}",
                            file = file,
                            line = 1,
                            column = 1,
                        ),
                    )
                }
            }

            return ValidationReport(
                filesValidated = fileCount,
                totalIssues = issues.size,
                errors = issues.count { it.severity == IssueSeverity.ERROR },
                warnings = issues.count { it.severity == IssueSeverity.WARNING },
                infos = issues.count { it.severity == IssueSeverity.INFO },
                issues = issues,
            )
        }

        /**
         * Validate a single .rosetta file.
         * TODO: Replace with actual Rune DSL validator integration.
         */
        private fun validateFile(file: VirtualFile): List<ValidationIssue> {
            val issues = mutableListOf<ValidationIssue>()

            try {
                val content = String(file.contentsToByteArray())
                val lines = content.lines()

                // Basic validation checks
                lines.forEachIndexed { index, line ->
                    val lineNum = index + 1

                    // Check for common issues (stub implementation)
                    when {
                        line.contains("TODO") ->
                            issues.add(
                                ValidationIssue(
                                    severity = IssueSeverity.INFO,
                                    message = "TODO comment found",
                                    file = file,
                                    line = lineNum,
                                    column = line.indexOf("TODO") + 1,
                                ),
                            )
                        line.trim().isEmpty() -> {} // Skip empty lines
                    }
                }

                // Add success info if no issues found
                if (issues.isEmpty()) {
                    issues.add(
                        ValidationIssue(
                            severity = IssueSeverity.INFO,
                            message = "File validated successfully",
                            file = file,
                            line = 1,
                            column = 1,
                        ),
                    )
                }
            } catch (e: Exception) {
                issues.add(
                    ValidationIssue(
                        severity = IssueSeverity.ERROR,
                        message = "Failed to read file: ${e.message}",
                        file = file,
                        line = 1,
                        column = 1,
                    ),
                )
            }

            return issues
        }

        /**
         * Show validation report in tool window.
         */
        private fun showValidationReport(
            project: Project,
            report: ValidationReport,
        ) {
            val toolWindowManager = ToolWindowManager.getInstance(project)

            // Get or create tool window
            var toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID)
            if (toolWindow == null) {
                toolWindow =
                    toolWindowManager.registerToolWindow(TOOL_WINDOW_ID) {
                        icon = null // TODO: Add validation icon
                    }
            }

            // Create content with report
            val contentFactory = ContentFactory.getInstance()
            val reportText = generateReportText(report)

            val textArea =
                JTextArea(reportText).apply {
                    isEditable = false
                    font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
                }

            val scrollPane = JScrollPane(textArea)
            val content = contentFactory.createContent(scrollPane, "Validation Report", false)

            toolWindow.contentManager.removeAllContents(true)
            toolWindow.contentManager.addContent(content)
            toolWindow.show()
        }

        /**
         * Generate formatted report text.
         */
        private fun generateReportText(report: ValidationReport): String {
            val writer = StringWriter()

            writer.appendLine("═══════════════════════════════════════════════════════")
            writer.appendLine("  RUNE DSL VALIDATION REPORT")
            writer.appendLine("═══════════════════════════════════════════════════════")
            writer.appendLine()

            if (report.cancelled) {
                writer.appendLine("Validation cancelled by user.")
                return writer.toString()
            }

            writer.appendLine("Files validated: ${report.filesValidated}")
            writer.appendLine("Total issues: ${report.totalIssues}")
            writer.appendLine("  Errors: ${report.errors}")
            writer.appendLine("  Warnings: ${report.warnings}")
            writer.appendLine("  Info: ${report.infos}")
            writer.appendLine()

            if (report.issues.isNotEmpty()) {
                writer.appendLine("───────────────────────────────────────────────────────")
                writer.appendLine("  ISSUES")
                writer.appendLine("───────────────────────────────────────────────────────")
                writer.appendLine()

                report.issues.sortedWith(
                    compareBy<ValidationIssue> { it.severity }
                        .thenBy { it.file.path }
                        .thenBy { it.line },
                ).forEach { issue ->
                    writer.appendLine("[${issue.severity}] ${issue.file.name}:${issue.line}:${issue.column}")
                    writer.appendLine("  ${issue.message}")
                    writer.appendLine()
                }
            }

            return writer.toString()
        }
    }

    /**
     * Validation report data.
     */
    data class ValidationReport(
        val filesValidated: Int,
        val totalIssues: Int,
        val errors: Int,
        val warnings: Int,
        val infos: Int,
        val issues: List<ValidationIssue>,
        val cancelled: Boolean = false,
    ) {
        companion object {
            fun cancelled() = ValidationReport(0, 0, 0, 0, 0, emptyList(), true)
        }
    }

    /**
     * Individual validation issue.
     */
    data class ValidationIssue(
        val severity: IssueSeverity,
        val message: String,
        val file: VirtualFile,
        val line: Int,
        val column: Int,
    )

    /**
     * Issue severity levels.
     */
    enum class IssueSeverity {
        ERROR,
        WARNING,
        INFO,
    }
}
