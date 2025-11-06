package com.github.pmouli.rune.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Project-level configuration for Rune DSL code generation.
 *
 * Stores settings such as:
 * - Output directory for generated Java files
 * - Package prefix
 * - Generator options (builders, validation, etc.)
 *
 * Thread-safe: IntelliJ Platform manages access via ReadAction/WriteAction.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "RuneProjectConfiguration",
    storages = [Storage("rune-dsl.xml")]
)
class RuneProjectConfiguration : PersistentStateComponent<RuneProjectConfiguration.State> {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): RuneProjectConfiguration {
            return project.getService(RuneProjectConfiguration::class.java)
        }
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    /**
     * Configuration state (serialized to XML).
     */
    data class State(
        var outputDirectory: String = "build/generated/sources/rune",
        var outputPackage: String = "com.example.generated",
        var generateBuilders: Boolean = true,
        var generateValidation: Boolean = true,
        var generatorId: String = "rune-java-generator",
        var grammarVersion: String = "5.0.0"
    )

    // Convenience accessors
    var outputDirectory: String
        get() = state.outputDirectory
        set(value) {
            state.outputDirectory = value
        }

    var outputPackage: String
        get() = state.outputPackage
        set(value) {
            state.outputPackage = value
        }

    var generateBuilders: Boolean
        get() = state.generateBuilders
        set(value) {
            state.generateBuilders = value
        }

    var generateValidation: Boolean
        get() = state.generateValidation
        set(value) {
            state.generateValidation = value
        }

    var generatorId: String
        get() = state.generatorId
        set(value) {
            state.generatorId = value
        }

    var grammarVersion: String
        get() = state.grammarVersion
        set(value) {
            state.grammarVersion = value
        }
}
