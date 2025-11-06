# Data Model: Rune DSL IntelliJ Plugin

**Branch**: `001-rune-dsl-intellij` | **Date**: 2025-01-21 | **Spec**: [spec.md](./spec.md)
**Phase**: 1 (Design) | **Output**: Entity definitions, PSI hierarchy, validation rules

## 1. Core Entities

### 1.1 DSL Document
**Description**: Represents a single .rosetta file with parsed content and validation state

**Attributes**:
- `filePath: VirtualFile` - IntelliJ VFS file reference
- `psiFile: RosettaPsiFile` - Root PSI element
- `namespace: String?` - Optional namespace declaration (e.g., "com.example.model")
- `imports: List<RosettaImport>` - Import statements
- `rootElements: List<RosettaRootElement>` - Top-level declarations (types, functions, enums, etc.)
- `grammarVersion: GrammarVersion` - Rune DSL version used in file
- `diagnostics: DiagnosticsReport` - Validation errors/warnings

**Validation Rules**:
- Namespace must match directory structure if present (e.g., "com.example" → src/main/rosetta/com/example/)
- No duplicate root element names within same namespace
- All imports must resolve to valid .rosetta files
- File size <1MB (performance constraint for PSI parsing)

**PSI Mapping**:
```kotlin
interface RosettaPsiFile : PsiFile {
    val namespace: RosettaNamespace?
    val imports: List<RosettaImport>
    val rootElements: List<RosettaRootElement>
}
```

---

### 1.2 Grammar Version
**Description**: Tracks Rune DSL grammar version for compatibility and feature detection

**Attributes**:
- `majorVersion: Int` - Major version (e.g., 5 for rune-dsl 5.0.0)
- `minorVersion: Int` - Minor version
- `patchVersion: Int` - Patch version
- `grammarHash: String` - SHA-256 of Rosetta.xtext for exact grammar matching
- `supportedFeatures: Set<LanguageFeature>` - Features available in this version (e.g., "pattern literals", "switch expressions")

**Validation Rules**:
- Plugin supports grammar versions 5.0.0-5.99.99 (untilBuild equivalent for grammar)
- Warn if file uses features unavailable in declared grammar version
- Error if grammar version < 5.0.0 (below minimum supported)

**PSI Mapping**:
```kotlin
// Inferred from file content and plugin configuration
data class GrammarVersion(val major: Int, val minor: Int, val patch: Int) {
    val versionString: String get() = "$major.$minor.$patch"
}
```

---

### 1.3 Project Configuration
**Description**: Rune DSL project settings for build/generation integration

**Attributes**:
- `projectRoot: VirtualFile` - Project base directory
- `sourceRoots: List<VirtualFile>` - Directories containing .rosetta files (e.g., src/main/rosetta)
- `outputDir: VirtualFile` - Generated Java sources directory (e.g., build/generated/sources/rune)
- `buildSystem: BuildSystem` - Maven, Gradle, or IntelliJ native
- `generatorProfiles: List<GeneratorProfile>` - Code generation configurations
- `dependencies: List<ProjectDependency>` - External Rune DSL libraries (imported namespaces)

**Validation Rules**:
- Source roots must exist and be readable
- Output directory must be writable
- No circular dependencies between Rune projects
- Generator profiles must reference valid Rune DSL generator JARs

**Storage**: Persisted in `.idea/rune-dsl-plugin.xml` (IntelliJ project settings)

---

### 1.4 Generator Profile
**Description**: Configuration for Rune DSL → Java code generation

**Attributes**:
- `name: String` - Profile identifier (e.g., "default", "test-models")
- `enabled: Boolean` - Whether to run generator on build
- `generatorClass: String` - Fully-qualified Java class name (e.g., "com.regnosys.rosetta.generator.java.RosettaJavaGenerator")
- `outputPackage: String` - Java package prefix for generated classes
- `options: Map<String, String>` - Generator-specific settings (e.g., "generateBuilders=true")
- `includePatterns: List<Glob>` - Files to include (e.g., "src/main/rosetta/**/*.rosetta")
- `excludePatterns: List<Glob>` - Files to exclude (e.g., "src/test/**")

**Validation Rules**:
- Generator class must be on classpath (check via Java reflection)
- Output package must be valid Java package name
- Include/exclude patterns must not overlap
- Options keys must match generator's supported parameters

**Example**:
```json
{
  "name": "default",
  "enabled": true,
  "generatorClass": "com.regnosys.rosetta.generator.java.RosettaJavaGenerator",
  "outputPackage": "com.example.generated",
  "options": {
    "generateBuilders": "true",
    "generateValidators": "false"
  },
  "includePatterns": ["src/main/rosetta/**/*.rosetta"],
  "excludePatterns": []
}
```

---

### 1.5 Diagnostics Report
**Description**: Validation errors, warnings, and info messages for a DSL document

**Attributes**:
- `documentPath: VirtualFile` - File being validated
- `errors: List<Diagnostic>` - Blocking issues (e.g., syntax errors, unresolved references)
- `warnings: List<Diagnostic>` - Non-blocking issues (e.g., unused imports, deprecated features)
- `infos: List<Diagnostic>` - Informational messages (e.g., "Type 'Foo' has no documentation")
- `timestamp: Instant` - When validation ran

**Diagnostic Structure**:
```kotlin
data class Diagnostic(
    val severity: DiagnosticSeverity, // ERROR, WARNING, INFO
    val range: TextRange,              // PSI offset range
    val message: String,
    val code: String?,                 // Error code (e.g., "RUNE-001" for unresolved reference)
    val quickFixes: List<QuickFix>     // Optional fixes (e.g., "Add import", "Create type")
)
```

**Validation Rules**:
- Diagnostics sorted by severity (errors first), then by offset
- No duplicate diagnostics for same range
- Quick fixes must be executable (implement `IntentionAction`)

**Performance**: Compute diagnostics incrementally in background thread; cache results per file

---

### 1.6 Generated Artifact
**Description**: Tracks Java source files generated from Rune DSL models

**Attributes**:
- `sourceFile: VirtualFile` - Original .rosetta file
- `generatedFile: VirtualFile` - Output .java file
- `generatorProfile: GeneratorProfile` - Profile used for generation
- `generatedTimestamp: Instant` - When file was generated
- `sourceHash: String` - SHA-256 of .rosetta content at generation time

**Validation Rules**:
- Generated file must be under configured output directory
- Source hash must match current .rosetta content (stale detection)
- Generated file must not be manually edited (warn if modified)

**PSI Mapping**:
```kotlin
data class GeneratedArtifact(
    val sourceFile: VirtualFile,
    val generatedFile: VirtualFile,
    val isStale: Boolean // sourceHash != current file hash
)
```

**Usage**: Show "Re-generate" gutter icon in .rosetta editor if artifacts are stale

---

## 2. PSI Hierarchy

### 2.1 Core PSI Elements

```kotlin
// Base interfaces
interface RosettaPsiElement : PsiElement
interface RosettaNamedElement : RosettaPsiElement, PsiNamedElement

// Root elements
interface RosettaRootElement : RosettaNamedElement
class RosettaDataType : RosettaRootElement // type Foo: ...
class RosettaFunction : RosettaRootElement  // func Bar: ...
class RosettaEnum : RosettaRootElement      // enum Baz: ...
class RosettaChoice : RosettaRootElement    // choice Qux: ...
class RosettaRule : RosettaRootElement      // eligibility rule R: ...

// Type members
class RosettaAttribute : RosettaNamedElement // attrName string (1..1)
class RosettaCondition : RosettaPsiElement   // condition Valid: ...

// Expressions
interface RosettaExpression : RosettaPsiElement
class RosettaFeatureCall : RosettaExpression // expr.field
class RosettaLiteral : RosettaExpression     // "string", 42, true
class RosettaIfExpression : RosettaExpression // if x then y else z
class RosettaSwitchExpression : RosettaExpression // x switch case A then ...

// References
class RosettaTypeReference : RosettaPsiElement // references RosettaDataType
class RosettaAttributeReference : RosettaPsiElement // references RosettaAttribute
```

### 2.2 PSI Element Methods

**RosettaDataType**:
```kotlin
interface RosettaDataType : RosettaRootElement {
    val attributes: List<RosettaAttribute>
    val conditions: List<RosettaCondition>
    val superTypes: List<RosettaDataType> // extends clause
}
```

**RosettaFunction**:
```kotlin
interface RosettaFunction : RosettaRootElement {
    val inputs: List<RosettaAttribute>
    val output: RosettaAttribute
    val operations: List<RosettaOperation> // set, add, etc.
}
```

**RosettaAttribute**:
```kotlin
interface RosettaAttribute : RosettaNamedElement {
    val typeReference: RosettaTypeReference
    val cardinality: RosettaCardinality // (0..1), (1..1), (0..*), etc.
    val annotations: List<RosettaAnnotation>
}
```

---

## 3. Validation Rules Summary

| Entity                | Rule Category       | Example                                      |
|-----------------------|---------------------|----------------------------------------------|
| DSL Document          | Structural          | Namespace matches directory structure        |
| Grammar Version       | Compatibility       | Version >= 5.0.0, feature usage valid        |
| Project Configuration | Build Integration   | Source roots exist, output dir writable      |
| Generator Profile     | Code Generation     | Generator class on classpath, options valid  |
| Diagnostics Report    | Incremental Update  | Cache per file, re-validate on edit          |
| Generated Artifact    | Staleness Detection | SHA-256 hash matches current .rosetta content|

---

## 4. Cross-Entity Relationships

```text
DSL Document 1 ───> * Root Elements (DataType, Function, Enum, etc.)
DSL Document 1 ───> 1 Grammar Version
DSL Document 1 ───> 1 Diagnostics Report

Project Configuration 1 ───> * Generator Profiles
Project Configuration 1 ───> * DSL Documents (via source roots)

Generator Profile 1 ───> * Generated Artifacts
Generated Artifact * ───> 1 DSL Document (source)
Generated Artifact * ───> 1 Generator Profile

DataType 1 ───> * Attributes
DataType * ───> * DataTypes (inheritance)
Function 1 ───> * Attributes (inputs/output)
Attribute 1 ───> 1 TypeReference ───> 1 DataType/Enum
```

---

## 5. State Management

### 5.1 PSI Lifecycle
- **Parse**: Lexer → Parser → PSI tree (on file open/edit)
- **Cache**: PSI tree cached by IntelliJ framework (automatic)
- **Invalidate**: On file edit, re-parse incrementally via `com.intellij.psi.PsiTreeChangeEvent`

### 5.2 Validation Lifecycle
- **Trigger**: Background thread after 300ms idle (debounced)
- **Compute**: `com.intellij.codeInsight.daemon.impl.AnnotatorRunner` runs `RosettaAnnotator`
- **Display**: Inline errors/warnings in editor gutter and Problems panel

### 5.3 Generation Lifecycle
- **Trigger**: Before-build compile task (`com.intellij.compiler.CompileTask`)
- **Preview**: Show diff view on manual invocation (Tools → Rune DSL → Generate Preview)
- **Apply**: Write generated files to VFS with `WriteAction`, trigger re-index

---

## Next Steps

1. **contracts/**: Define `RosettaGeneratorContract` interface for invoking code generators
2. **quickstart.md**: Document plugin installation → create Rune project → validate → generate
3. **Constitution Re-Check**: Validate PSI design against Threading/PSI safety, Architecture gates
