package com.github.pmouli.rune.generation

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Contract for invoking Rune DSL code generators.
 *
 * Implementations must be available on classpath (bundled or user-provided).
 * The plugin discovers generators via Java ServiceLoader or explicit registration.
 *
 * Thread-safe: Implementations should be stateless or thread-safe.
 */
interface RosettaGeneratorContract {
    /**
     * Metadata about this generator.
     */
    val metadata: GeneratorMetadata

    /**
     * Generate Java sources from Rune DSL models.
     *
     * @param request Generation parameters including source files and options
     * @return Results including generated files and diagnostics
     * @throws GeneratorException if generation fails critically
     */
    fun generate(request: GenerationRequest): GenerationResult
}

/**
 * Generator metadata for display and configuration.
 */
data class GeneratorMetadata(
    val id: String, // e.g., "rosetta-java-generator"
    val displayName: String, // e.g., "Rune DSL Java Generator"
    val version: String, // e.g., "5.0.0"
    val supportedOptions: Map<String, OptionDescriptor> = emptyMap(),
)

/**
 * Descriptor for a generator option/configuration parameter.
 */
data class OptionDescriptor(
    val key: String,
    val type: OptionType, // BOOLEAN, STRING, INT
    val defaultValue: String?,
    val description: String,
)

/**
 * Types of generator options.
 */
enum class OptionType { BOOLEAN, STRING, INT }

/**
 * Request for code generation.
 */
data class GenerationRequest(
    val project: Project,
    val sourceFiles: List<VirtualFile>, // .rosetta files to process
    val outputDirectory: Path, // target for generated .java files
    val outputPackage: String, // Java package prefix
    val options: Map<String, String> = emptyMap(), // generator-specific options
    val grammarVersion: String = "5.0.0", // Rune DSL grammar version
)

/**
 * Result of code generation.
 */
data class GenerationResult(
    val success: Boolean,
    val generatedFiles: List<GeneratedFile>,
    val diagnostics: List<GeneratorDiagnostic> = emptyList(),
)

/**
 * Information about a generated file.
 */
data class GeneratedFile(
    // original .rosetta file
    val sourcePath: Path,
    // generated .java file
    val outputPath: Path,
    // SHA-256 of generated content for change detection
    val checksum: String,
)

/**
 * Diagnostic message from generator.
 */
data class GeneratorDiagnostic(
    val severity: DiagnosticSeverity,
    val message: String,
    val sourceFile: Path? = null,
    val line: Int? = null,
    val column: Int? = null,
)

/**
 * Severity levels for diagnostics.
 */
enum class DiagnosticSeverity { ERROR, WARNING, INFO }

/**
 * Exception thrown when generation fails.
 */
class GeneratorException(message: String, cause: Throwable? = null) : Exception(message, cause)
