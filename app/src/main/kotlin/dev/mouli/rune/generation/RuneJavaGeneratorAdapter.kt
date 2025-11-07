package dev.mouli.rune.generation

import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Adapter for Rune DSL Java generator.
 *
 * This implementation wraps the Rune DSL Java generator (from rune-dsl Maven plugin)
 * and adapts it to the RosettaGeneratorContract interface.
 *
 * The actual Rune generator JAR must be on the classpath (bundled with plugin
 * or provided by user via project dependencies).
 *
 * Thread-safe: Stateless implementation delegates to Rune generator.
 */
class RuneJavaGeneratorAdapter : RosettaGeneratorContract {
    companion object {
        private val LOG = Logger.getInstance(RuneJavaGeneratorAdapter::class.java)
    }

    override val metadata =
        GeneratorMetadata(
            id = "rune-java-generator",
            displayName = "Rune DSL Java Generator",
            version = "5.0.0",
            supportedOptions =
                mapOf(
                    "generateBuilders" to
                        OptionDescriptor(
                            key = "generateBuilders",
                            type = OptionType.BOOLEAN,
                            defaultValue = "true",
                            description = "Generate builder classes for Rosetta types",
                        ),
                    "generateValidation" to
                        OptionDescriptor(
                            key = "generateValidation",
                            type = OptionType.BOOLEAN,
                            defaultValue = "true",
                            description = "Generate validation logic",
                        ),
                    "packagePrefix" to
                        OptionDescriptor(
                            key = "packagePrefix",
                            type = OptionType.STRING,
                            defaultValue = "com.example.generated",
                            description = "Java package prefix for generated classes",
                        ),
                ),
        )

    override fun generate(request: GenerationRequest): GenerationResult {
        LOG.info("Starting Rune DSL code generation for ${request.sourceFiles.size} files")

        return try {
            // TODO: Integrate with actual Rune DSL generator
            // For now, create a stub implementation that demonstrates the flow
            generateStub(request)
        } catch (e: Exception) {
            LOG.error("Code generation failed", e)
            GenerationResult(
                success = false,
                generatedFiles = emptyList(),
                diagnostics =
                    listOf(
                        GeneratorDiagnostic(
                            severity = DiagnosticSeverity.ERROR,
                            message = "Generation failed: ${e.message}",
                            sourceFile = null,
                            line = null,
                            column = null,
                        ),
                    ),
            )
        }
    }

    /**
     * Stub implementation that creates sample generated files.
     * This will be replaced with actual Rune DSL generator integration.
     */
    private fun generateStub(request: GenerationRequest): GenerationResult {
        val generatedFiles = mutableListOf<GeneratedFile>()
        val diagnostics = mutableListOf<GeneratorDiagnostic>()

        // Ensure output directory exists
        Files.createDirectories(request.outputDirectory)

        // Generate a stub Java file for each .rosetta file
        request.sourceFiles.forEach { sourceFile ->
            try {
                val sourcePath = Path.of(sourceFile.path)
                val fileName = sourceFile.nameWithoutExtension
                val javaFileName = "${fileName.replaceFirstChar { it.uppercase() }}.java"
                val outputPath = request.outputDirectory.resolve(javaFileName)

                // Generate stub Java class
                val javaContent =
                    generateStubJavaClass(
                        className = fileName.replaceFirstChar { it.uppercase() },
                        packageName = request.outputPackage,
                    )

                Files.writeString(outputPath, javaContent)

                // Calculate checksum
                val checksum = calculateChecksum(javaContent)

                generatedFiles.add(
                    GeneratedFile(
                        sourcePath = sourcePath,
                        outputPath = outputPath,
                        checksum = checksum,
                    ),
                )

                diagnostics.add(
                    GeneratorDiagnostic(
                        severity = DiagnosticSeverity.INFO,
                        message = "Generated $javaFileName",
                        sourceFile = sourcePath,
                        line = null,
                        column = null,
                    ),
                )
            } catch (e: Exception) {
                diagnostics.add(
                    GeneratorDiagnostic(
                        severity = DiagnosticSeverity.ERROR,
                        message = "Failed to generate from ${sourceFile.name}: ${e.message}",
                        sourceFile = Path.of(sourceFile.path),
                        line = null,
                        column = null,
                    ),
                )
            }
        }

        return GenerationResult(
            success = diagnostics.none { it.severity == DiagnosticSeverity.ERROR },
            generatedFiles = generatedFiles,
            diagnostics = diagnostics,
        )
    }

    private fun generateStubJavaClass(
        className: String,
        packageName: String,
    ): String {
        return """
            package $packageName;
            
            /**
             * Generated from Rune DSL.
             * This is a stub implementation - actual generator integration pending.
             */
            public class $className {
                // TODO: Add generated fields, methods, validation
                
                public $className() {
                    // Constructor
                }
            }
            """.trimIndent()
    }

    private fun calculateChecksum(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
