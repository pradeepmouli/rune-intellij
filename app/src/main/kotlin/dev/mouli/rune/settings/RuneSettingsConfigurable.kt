package dev.mouli.rune.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings UI for Rune DSL code generation.
 *
 * Provides configuration interface in Settings/Preferences dialog
 * under Tools > Rune DSL.
 *
 * Thread-safe: Runs on EDT by IntelliJ Platform.
 */
class RuneSettingsConfigurable(private val project: Project) : Configurable {
    private var panel: JPanel? = null
    private val outputDirField = JBTextField()
    private val outputPackageField = JBTextField()
    private val generateBuildersCheckbox = JBCheckBox("Generate Builder Classes")
    private val generateValidationCheckbox = JBCheckBox("Generate Validation Logic")

    override fun getDisplayName(): String = "Rune DSL"

    override fun createComponent(): JComponent {
        val config = RuneProjectConfiguration.getInstance(project)

        // Initialize fields with current config
        outputDirField.text = config.outputDirectory
        outputPackageField.text = config.outputPackage
        generateBuildersCheckbox.isSelected = config.generateBuilders
        generateValidationCheckbox.isSelected = config.generateValidation

        panel =
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Output Directory:", outputDirField)
                .addLabeledComponent("Output Package:", outputPackageField)
                .addComponent(generateBuildersCheckbox)
                .addComponent(generateValidationCheckbox)
                .addComponentFillVertically(JPanel(), 0)
                .panel

        return panel!!
    }

    override fun isModified(): Boolean {
        val config = RuneProjectConfiguration.getInstance(project)
        return outputDirField.text != config.outputDirectory ||
            outputPackageField.text != config.outputPackage ||
            generateBuildersCheckbox.isSelected != config.generateBuilders ||
            generateValidationCheckbox.isSelected != config.generateValidation
    }

    override fun apply() {
        val config = RuneProjectConfiguration.getInstance(project)
        config.outputDirectory = outputDirField.text
        config.outputPackage = outputPackageField.text
        config.generateBuilders = generateBuildersCheckbox.isSelected
        config.generateValidation = generateValidationCheckbox.isSelected
    }

    override fun reset() {
        val config = RuneProjectConfiguration.getInstance(project)
        outputDirField.text = config.outputDirectory
        outputPackageField.text = config.outputPackage
        generateBuildersCheckbox.isSelected = config.generateBuilders
        generateValidationCheckbox.isSelected = config.generateValidation
    }
}
