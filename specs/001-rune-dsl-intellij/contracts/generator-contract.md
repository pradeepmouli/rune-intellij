# Generator Contract: Rune DSL → Java Code Generation

**Version**: 1.0  
**Protocol**: Java Classpath Invocation (not LSP/RPC)  
**Purpose**: Invoke Rune DSL code generators from IntelliJ plugin during build

---

## Interface Definition

### Kotlin Interface (Plugin-Side)

```kotlin
package com.github.pmouli.rune

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Contract for invoking Rune DSL code generators.
 * Implementations must be on classpath (bundled or user-provided).
 */
interface RosettaGeneratorContract {
    /**
     * Metadata about this generator.
     */
    val metadata: GeneratorMetadata

    /**
     * Generate Java sources from Rune DSL models.
     *
     * @param request Generation parameters
     * @return Results including generated files and diagnostics
     * @throws GeneratorException if generation fails
     */
    fun generate(request: GenerationRequest): GenerationResult
}

data class GeneratorMetadata(
    val id: String,              // e.g., "rosetta-java-generator"
    val displayName: String,     // e.g., "Rune DSL Java Generator"
    val version: String,         // e.g., "5.0.0"
    val supportedOptions: Map<String, OptionDescriptor>
)

data class OptionDescriptor(
    val key: String,
    val type: OptionType,        // BOOLEAN, STRING, INT
    val defaultValue: String?,
    val description: String
)

enum class OptionType { BOOLEAN, STRING, INT }

data class GenerationRequest(
    val project: Project,
    val sourceFiles: List<VirtualFile>,  // .rosetta files to process
    val outputDirectory: Path,           // target for generated .java files
    val outputPackage: String,           // Java package prefix
    val options: Map<String, String>,    // generator-specific options
    val grammarVersion: String           // e.g., "5.0.0"
)

data class GenerationResult(
    val success: Boolean,
    val generatedFiles: List<GeneratedFile>,
    val diagnostics: List<GeneratorDiagnostic>
)

data class GeneratedFile(
    val sourcePath: Path,       // original .rosetta file
    val outputPath: Path,       // generated .java file
    val checksum: String        // SHA-256 of generated content
)

data class GeneratorDiagnostic(
    val severity: DiagnosticSeverity,
    val message: String,
    val sourceFile: Path?,
    val line: Int?,
    val column: Int?
)

enum class DiagnosticSeverity { ERROR, WARNING, INFO }

class GeneratorException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

---

## Reference Implementation (Adapter for Rune DSL Maven Generator)

```kotlin
package com.github.pmouli.rune.generators

import com.regnosys.rosetta.RosettaStandaloneSetup
import com.regnosys.rosetta.generator.RosettaGenerator
import com.regnosys.rosetta.maven.AbstractRuneGeneratorMojo
import com.github.pmouli.rune.*

/**
 * Adapter wrapping Rune DSL's Maven-based generator for IntelliJ plugin use.
 * Requires rune-lang and rune-maven-plugin JARs on classpath.
 */
class RuneJavaGeneratorAdapter : RosettaGeneratorContract {
    override val metadata = GeneratorMetadata(
        id = "rosetta-java-generator",
        displayName = "Rune DSL Java Generator",
        version = "5.0.0",
        supportedOptions = mapOf(
            "generateBuilders" to OptionDescriptor(
                key = "generateBuilders",
                type = OptionType.BOOLEAN,
                defaultValue = "true",
                description = "Generate builder classes for Rosetta types"
            ),
            "generateValidators" to OptionDescriptor(
                key = "generateValidators",
                type = OptionType.BOOLEAN,
                defaultValue = "false",
                description = "Generate validation methods"
            )
        )
    )

    override fun generate(request: GenerationRequest): GenerationResult {
        try {
            // 1. Initialize Xtext/EMF standalone setup
            val injector = RosettaStandaloneSetup().createInjectorAndDoEMFRegistration()
            val generator = injector.getInstance(RosettaGenerator::class.java)

            // 2. Parse .rosetta files into EMF models
            val resourceSet = // ... load .rosetta files via Xtext parser

            // 3. Invoke generator
            val generatedFiles = mutableListOf<GeneratedFile>()
            generator.doGenerate(resourceSet, /* output stream adapter */)

            // 4. Collect generated .java files
            // ... scan outputDirectory for new files

            return GenerationResult(
                success = true,
                generatedFiles = generatedFiles,
                diagnostics = emptyList()
            )
        } catch (e: Exception) {
            throw GeneratorException("Generation failed: ${e.message}", e)
        }
    }
}
```

---

## Discovery Mechanism

Plugin discovers generators via:

1. **Bundled Generator**: `RuneJavaGeneratorAdapter` shipped with plugin
2. **Classpath Scan**: `ServiceLoader` for user-provided generators
3. **Settings UI**: User configures generator in "Settings → Rune DSL → Generators"

```kotlin
// Plugin initialization
object GeneratorRegistry {
    private val generators = mutableMapOf<String, RosettaGeneratorContract>()

    fun register(generator: RosettaGeneratorContract) {
        generators[generator.metadata.id] = generator
    }

    fun getGenerator(id: String): RosettaGeneratorContract? = generators[id]

    fun discoverGenerators() {
        // Auto-register bundled generator
        register(RuneJavaGeneratorAdapter())

        // Scan classpath for user-provided generators
        ServiceLoader.load(RosettaGeneratorContract::class.java).forEach { register(it) }
    }
}
```

---

## Usage Example (Plugin → Generator)

```kotlin
// In CompileTask implementation
class RuneGenerationCompileTask : CompileTask {
    override fun execute(context: CompileContext): Boolean {
        val project = context.project
        val config = RuneProjectConfiguration.getInstance(project)

        config.generatorProfiles.filter { it.enabled }.forEach { profile ->
            val generator = GeneratorRegistry.getGenerator(profile.generatorClass)
                ?: throw IllegalStateException("Generator not found: ${profile.generatorClass}")

            val sourceFiles = // ... collect .rosetta files matching profile.includePatterns

            val request = GenerationRequest(
                project = project,
                sourceFiles = sourceFiles,
                outputDirectory = config.outputDir.toNioPath(),
                outputPackage = profile.outputPackage,
                options = profile.options,
                grammarVersion = "5.0.0"
            )

            val result = generator.generate(request)

            if (!result.success) {
                result.diagnostics.forEach { diagnostic ->
                    context.addMessage(/* convert to CompilerMessage */)
                }
                return false
            }

            // Trigger VFS refresh for generated files
            VfsUtil.markDirtyAndRefresh(/* ... */)
        }

        return true
    }
}
```

---

## Error Handling

| Error Condition                | Generator Behavior                              | Plugin Behavior                          |
|--------------------------------|-------------------------------------------------|------------------------------------------|
| Invalid .rosetta syntax        | Return `GeneratorDiagnostic` with ERROR         | Show in Problems panel, fail build       |
| Missing dependency reference   | Throw `GeneratorException`                      | Show error notification, fail build      |
| Output directory not writable  | Throw `GeneratorException`                      | Show error notification, halt generation |
| Unknown generator option       | Log warning, ignore option                      | Validate against `supportedOptions`      |
| Generator version mismatch     | Throw `GeneratorException` if incompatible      | Check `metadata.version` vs grammar      |

---

## Performance Considerations

1. **Incremental Generation**: Only regenerate files whose source .rosetta files changed (compare `checksum`)
2. **Parallelism**: Generate multiple files concurrently (up to `Runtime.availableProcessors()`)
3. **Caching**: Cache EMF resource sets between builds (invalidate on .rosetta edit)

---

## Future Extensions

1. **TypeScript Generator**: Additional `RosettaTypeScriptGeneratorAdapter` for TS output
2. **Custom Templates**: Allow users to provide Velocity/Freemarker templates
3. **Gradle Plugin**: Standalone `com.github.pmouli.rune-gradle-plugin` invoking same contract
