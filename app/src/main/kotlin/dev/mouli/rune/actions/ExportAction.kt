package dev.mouli.rune.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Action to export Rune DSL artifacts.
 *
 * Exports .rosetta source files and optionally generated code as:
 * - ZIP archive
 * - Directory structure
 * - Maven/Gradle project (with build configuration)
 *
 * User can choose export format and destination via dialog.
 */
class ExportAction : AnAction(), DumbAware {
    companion object {
        private val LOG = Logger.getInstance(ExportAction::class.java)
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

        // Show export options dialog
        val options = showExportOptionsDialog(project) ?: return

        // Select destination
        val destination = selectExportDestination(project) ?: return

        // Run export in background
        ProgressManager.getInstance().run(ExportTask(project, options, destination))
    }

    /**
     * Show dialog to configure export options.
     */
    private fun showExportOptionsDialog(project: Project): ExportOptions? {
        // TODO: Create custom dialog for export options
        // For now, use simple message dialog to confirm

        val options = arrayOf("ZIP Archive", "Directory", "Maven Project")
        val choice =
            Messages.showChooseDialog(
                project,
                "Select export format:",
                "Export Rune DSL Artifacts",
                Messages.getQuestionIcon(),
                options,
                options[0],
            )

        if (choice < 0) return null

        val format =
            when (choice) {
                0 -> ExportFormat.ZIP
                1 -> ExportFormat.DIRECTORY
                2 -> ExportFormat.MAVEN_PROJECT
                else -> return null
            }

        val includeGenerated =
            Messages.showYesNoDialog(
                project,
                "Include generated code?",
                "Export Options",
                Messages.getQuestionIcon(),
            ) == Messages.YES

        return ExportOptions(format, includeGenerated)
    }

    /**
     * Show file chooser to select export destination.
     */
    private fun selectExportDestination(project: Project): File? {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        descriptor.title = "Select Export Destination"
        descriptor.description = "Choose where to export Rune DSL artifacts"

        val chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, project, null)
        val selected = chooser.choose(project)

        return selected.firstOrNull()?.let { File(it.path) }
    }

    /**
     * Background task for exporting files.
     */
    private class ExportTask(
        project: Project,
        private val options: ExportOptions,
        private val destination: File,
    ) : Task.Backgroundable(project, "Exporting Rune DSL Artifacts", true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text = "Collecting files..."
            indicator.isIndeterminate = false

            // Find all .rosetta files
            val rosettaFiles = findRosettaFiles(project)

            if (rosettaFiles.isEmpty()) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showInfoMessage(
                        project,
                        "No .rosetta files found to export.",
                        "Export",
                    )
                }
                return
            }

            indicator.text = "Exporting ${rosettaFiles.size} file(s)..."
            indicator.fraction = 0.2

            try {
                when (options.format) {
                    ExportFormat.ZIP -> exportAsZip(rosettaFiles, indicator)
                    ExportFormat.DIRECTORY -> exportAsDirectory(rosettaFiles, indicator)
                    ExportFormat.MAVEN_PROJECT -> exportAsMavenProject(rosettaFiles, indicator)
                }

                indicator.fraction = 1.0

                ApplicationManager.getApplication().invokeLater {
                    Messages.showInfoMessage(
                        project,
                        "Successfully exported ${rosettaFiles.size} file(s) to:\n${destination.absolutePath}",
                        "Export Complete",
                    )
                }
            } catch (e: Exception) {
                LOG.error("Export failed", e)
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Export failed: ${e.message}",
                        "Export Error",
                    )
                }
            }
        }

        /**
         * Find all .rosetta files in project.
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
         * Export as ZIP archive.
         */
        private fun exportAsZip(
            files: List<VirtualFile>,
            indicator: ProgressIndicator,
        ) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val zipFile = File(destination, "rune-export-$timestamp.zip")

            ZipOutputStream(zipFile.outputStream()).use { zip ->
                files.forEachIndexed { index, file ->
                    indicator.fraction = 0.3 + (index.toDouble() / files.size) * 0.6
                    indicator.text2 = "Adding ${file.name}..."

                    val entry = ZipEntry("rosetta/${file.name}")
                    zip.putNextEntry(entry)
                    zip.write(file.contentsToByteArray())
                    zip.closeEntry()
                }
            }

            LOG.info("Exported to ZIP: ${zipFile.absolutePath}")
        }

        /**
         * Export as directory structure.
         */
        private fun exportAsDirectory(
            files: List<VirtualFile>,
            indicator: ProgressIndicator,
        ) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val exportDir = File(destination, "rune-export-$timestamp")
            val rosettaDir = File(exportDir, "rosetta")
            rosettaDir.mkdirs()

            files.forEachIndexed { index, file ->
                indicator.fraction = 0.3 + (index.toDouble() / files.size) * 0.6
                indicator.text2 = "Copying ${file.name}..."

                val targetFile = File(rosettaDir, file.name)
                Files.copy(
                    File(file.path).toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }

            // Create README
            val readme = File(exportDir, "README.md")
            readme.writeText(
                """
                # Rune DSL Export

                Exported on: ${LocalDateTime.now()}
                Files: ${files.size}

                ## Contents

                - rosetta/ - Rune DSL source files

                """.trimIndent(),
            )

            LOG.info("Exported to directory: ${exportDir.absolutePath}")
        }

        /**
         * Export as Maven project with build configuration.
         */
        private fun exportAsMavenProject(
            files: List<VirtualFile>,
            indicator: ProgressIndicator,
        ) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val projectDir = File(destination, "rune-project-$timestamp")
            val srcDir = File(projectDir, "src/main/rosetta")
            srcDir.mkdirs()

            // Copy .rosetta files
            files.forEachIndexed { index, file ->
                indicator.fraction = 0.3 + (index.toDouble() / files.size) * 0.4
                indicator.text2 = "Copying ${file.name}..."

                val targetFile = File(srcDir, file.name)
                Files.copy(
                    File(file.path).toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }

            indicator.text = "Creating Maven configuration..."
            indicator.fraction = 0.7

            // Create pom.xml
            val pom = File(projectDir, "pom.xml")
            pom.writeText(createPomXml())

            // Create README
            val readme = File(projectDir, "README.md")
            readme.writeText(createMavenReadme())

            indicator.fraction = 0.9

            LOG.info("Exported as Maven project: ${projectDir.absolutePath}")
        }

        /**
         * Create pom.xml for Maven project export.
         */
        private fun createPomXml(): String {
            return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.example</groupId>
                    <artifactId>rune-project</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <properties>
                        <maven.compiler.source>11</maven.compiler.source>
                        <maven.compiler.target>11</maven.compiler.target>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <rune.version>5.0.0</rune.version>
                    </properties>

                    <dependencies>
                        <!-- Rune DSL dependencies would go here -->
                    </dependencies>

                    <build>
                        <plugins>
                            <!-- Rune DSL Maven plugin configuration would go here -->
                        </plugins>
                    </build>
                </project>
                """.trimIndent()
        }

        /**
         * Create README for Maven project export.
         */
        private fun createMavenReadme(): String {
            return """
                # Rune DSL Maven Project

                Exported on: ${LocalDateTime.now()}

                ## Building

                ```bash
                mvn clean compile
                ```

                ## Project Structure

                - src/main/rosetta/ - Rune DSL source files
                - pom.xml - Maven build configuration

                ## Next Steps

                1. Update groupId, artifactId, and version in pom.xml
                2. Configure Rune DSL Maven plugin
                3. Add project dependencies
                4. Run `mvn compile` to generate Java code

                """.trimIndent()
        }
    }

    /**
     * Export options.
     */
    data class ExportOptions(
        val format: ExportFormat,
        val includeGenerated: Boolean,
    )

    /**
     * Export format options.
     */
    enum class ExportFormat {
        ZIP,
        DIRECTORY,
        MAVEN_PROJECT,
    }
}
