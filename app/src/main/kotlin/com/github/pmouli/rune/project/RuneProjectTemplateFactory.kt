package com.github.pmouli.rune.project

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory
import javax.swing.Icon

/**
 * Factory for creating Rune DSL project templates.
 *
 * Provides project/module wizard templates for initializing new Rune DSL projects
 * with standard structure and configuration.
 */
class RuneProjectTemplateFactory : ProjectTemplatesFactory() {
    override fun getGroups(): Array<String> = arrayOf("Rune DSL")

    override fun createTemplates(
        group: String?,
        context: WizardContext,
    ): Array<ProjectTemplate> {
        return arrayOf(RuneProjectTemplate())
    }
}

/**
 * Project template for Rune DSL projects.
 *
 * Creates a new project with standard Rune DSL structure including:
 * - src/main/rosetta/ for .rosetta source files
 * - src/generated/java/ for generated code
 * - Sample .rosetta file
 * - Basic configuration
 */
class RuneProjectTemplate : ProjectTemplate {
    override fun getName(): String = "Rune DSL Project"

    override fun getDescription(): String = "Creates a new Rune DSL project with standard structure and sample files"

    override fun getIcon(): Icon? = null // TODO: Add template icon

    override fun createModuleBuilder(): ModuleBuilder = RuneModuleBuilder()

    override fun validateSettings() = null
}

/**
 * Module builder for Rune DSL projects.
 *
 * Sets up the project structure and creates initial files.
 */
class RuneModuleBuilder : ModuleBuilder() {
    override fun getModuleType(): RuneModuleType = RuneModuleType.getInstance()

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val project = modifiableRootModel.project
        val module = modifiableRootModel.module

        // Get content root
        val contentEntry = doAddContentEntry(modifiableRootModel) ?: return
        val contentRoot = contentEntry.file ?: return

        // Create standard directory structure
        createProjectStructure(project, contentRoot)

        // Create sample files
        createSampleFiles(project, contentRoot)
    }

    /**
     * Create standard Rune DSL project directory structure.
     */
    private fun createProjectStructure(
        project: Project,
        contentRoot: VirtualFile,
    ) {
        val directories =
            listOf(
                "src/main/rosetta",
                "src/generated/java",
                "src/test/rosetta",
            )

        directories.forEach { dir ->
            val path = "${contentRoot.path}/$dir"
            FileUtil.createDirectory(java.io.File(path))
        }

        // Refresh VFS to make directories visible
        LocalFileSystem.getInstance().refreshAndFindFileByPath(contentRoot.path)
    }

    /**
     * Create sample Rune DSL files to help users get started.
     */
    private fun createSampleFiles(
        project: Project,
        contentRoot: VirtualFile,
    ) {
        val rosettaDir =
            LocalFileSystem.getInstance()
                .refreshAndFindFileByPath("${contentRoot.path}/src/main/rosetta")
                ?: return

        // Create sample .rosetta file
        val sampleContent =
            """
            namespace "com.example.model"
            version "1.0.0"

            /*
             * Sample Rune DSL type definition.
             * Edit this file to define your domain model.
             */
            type Person:
                firstName string (1..1)
                lastName string (1..1)
                age int (0..1)
                email string (0..*)

            type Address:
                street string (1..1)
                city string (1..1)
                state string (0..1)
                zipCode string (1..1)

            /*
             * Sample enum definition
             */
            enum ContactMethod:
                EMAIL
                PHONE
                MAIL
            """.trimIndent()

        try {
            val sampleFile = rosettaDir.createChildData(this, "sample.rosetta")
            sampleFile.setBinaryContent(sampleContent.toByteArray())
        } catch (e: Exception) {
            // Log error but don't fail project creation
            println("Failed to create sample file: ${e.message}")
        }

        // Create README
        createReadme(contentRoot)
    }

    /**
     * Create project README with instructions.
     */
    private fun createReadme(contentRoot: VirtualFile) {
        val readmeContent =
            """
            # Rune DSL Project

            This project uses the Rune DSL (Domain Specific Language) for modeling.

            ## Project Structure

            - `src/main/rosetta/` - Your .rosetta source files
            - `src/generated/java/` - Generated Java code (do not edit manually)
            - `src/test/rosetta/` - Test .rosetta files

            ## Getting Started

            1. Edit the sample .rosetta file in `src/main/rosetta/`
            2. Define your domain model using Rune DSL syntax
            3. Use the "Generate from Rune" action to generate Java code
            4. Review and apply the generated code

            ## Rune DSL Syntax

            Basic constructs:
            - `type` - Define data types
            - `enum` - Define enumerations
            - `func` - Define functions
            - Attributes use `name type (cardinality)`

            ## Resources

            - [Rune DSL Documentation](https://github.com/finos/rune-dsl)
            - [VS Code Extension](https://github.com/finos/rune-dsl/tree/main/rune-ide)

            """.trimIndent()

        try {
            val readme = contentRoot.createChildData(this, "README.md")
            readme.setBinaryContent(readmeContent.toByteArray())
        } catch (e: Exception) {
            println("Failed to create README: ${e.message}")
        }
    }

    override fun getCustomOptionsStep(
        context: WizardContext,
        parentDisposable: Disposable,
    ): ModuleWizardStep? {
        // Could add custom configuration step here if needed
        return null
    }
}
