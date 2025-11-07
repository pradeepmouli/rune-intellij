# Research: Rune DSL IntelliJ Plugin

**Branch**: `001-rune-dsl-intellij` | **Date**: 2025-01-21 | **Spec**: [spec.md](./spec.md)
**Phase**: 0 (Research) | **Output**: Technical decisions, alternatives, rationale

## Research Questions

1. **Rune DSL Grammar Integration**: How to integrate Rune DSL grammar (Xtext/ANTLR-based) into IntelliJ Platform?
2. **IntelliJ Language Support Best Practices**: What are IntelliJ Platform APIs/patterns for lexer/parser/PSI/language features?
3. **Code Generation Architecture**: How to integrate Rune code generators (Rosie/Java output) with IntelliJ build system?
4. **VS Code Extension Parity Analysis**: What features from rune-ide VS Code extension must be replicated in IntelliJ?

---

## 1. Rune DSL Grammar Integration

### Context
Rune DSL (https://github.com/finos/rune-dsl) is built with:
- **Xtext 2.39** framework for language definition
- **Xcore** (EMF-based) models for AST representation
- **ANTLR**-like parser generation from Rosetta.xtext grammar
- **TextMate grammar** for VS Code syntax highlighting (rune-ide/src/main/java/com/regnosys/rosetta/ide/textmate/GenerateTmGrammar.java)

### Decision: Custom IntelliJ Lexer/Parser (Not Xtext Runtime)
**Rationale**:
- IntelliJ Platform doesn't support Xtext runtime; requires native Lexer/ParserDefinition implementations
- VS Code extension uses LSP (Language Server Protocol) via RosettaLanguageServerImpl; IntelliJ uses PSI-based model
- Existing Rune DSL grammar (Rosetta.xtext) defines 30-40 language constructs (type, func, choice, enum, namespace, etc.)

**Approach**:
1. **Lexer**: Implement `com.intellij.lexer.Lexer` for Rune DSL tokens (keywords, identifiers, operators, comments)
   - Reference: Xtext grammar terminals (ID, STRING, NUMBER, PATTERN, etc.)
   - Use JetBrains Grammar-Kit plugin to generate lexer from .flex file (similar to TextMate grammar conversion)
2. **Parser**: Implement `com.intellij.lang.PsiParser` to build PSI tree from tokens
   - Mirror Xtext Rosetta grammar structure (RosettaModel → Namespace → RootElement → Data/Function/Enum/etc.)
   - Use IntelliJ Platform's `com.intellij.lang.PsiBuilder` for incremental parsing
3. **PSI Tree**: Define PSI elements hierarchy (`RosettaPsiElement`, `RosettaDataType`, `RosettaFunction`, etc.)
   - Map to Xcore models (com/regnosys/rosetta/rosetta/RosettaPackage.java)
   - Implement `PsiNamedElement` for symbols (types, functions, attributes)

**Alternatives Considered**:
- **Xtext Runtime in IntelliJ**: Rejected; Xtext is Eclipse-based, IntelliJ requires native PSI model
- **LSP Client Plugin**: Rejected; would reuse VS Code language server but lose IntelliJ-specific features (e.g., Kotlin interop, custom inspections)
- **TextMate Bundled Grammar**: Partial solution; provides syntax highlighting only, no semantic analysis

**Trade-offs**:
- ✅ Full IntelliJ Platform integration (PSI, refactoring, debugging)
- ❌ Requires manual grammar conversion from Xtext to IntelliJ .flex + .bnf
- ❌ Maintenance burden: keep IntelliJ grammar in sync with Rune DSL evolution

**References**:
- IntelliJ Platform Language Support: https://plugins.jetbrains.com/docs/intellij/custom-language-support.html
- Grammar-Kit plugin: https://github.com/JetBrains/Grammar-Kit
- Rune DSL Xtext grammar: finos/rune-dsl/rune-lang/src/main/java/com.regnosys.rosetta.Rosetta.xtext

---

## 2. IntelliJ Language Support Best Practices

### Context
IntelliJ Platform provides:
- **PSI (Program Structure Interface)**: Tree model for code analysis
- **Stub indexes**: Fast symbol lookup without parsing entire project
- **Language features**: Completion, navigation, inspections, formatting, etc.

### Decisions

#### 2.1 PSI Tree Design
**Approach**:
- Extend `com.intellij.extapi.psi.ASTWrapperPsiElement` for non-leaf nodes
- Extend `com.intellij.psi.PsiNamedElement` for symbols (types, functions, attributes)
- Use `com.intellij.psi.util.PsiTreeUtil` for tree navigation
- Implement `com.intellij.psi.PsiReference` for cross-file symbol resolution

**Example Hierarchy**:
```kotlin
interface RosettaPsiElement : PsiElement
interface RosettaNamedElement : RosettaPsiElement, PsiNamedElement
class RosettaDataType : RosettaNamedElement // type Foo: ...
class RosettaFunction : RosettaNamedElement  // func Bar: ...
class RosettaAttribute : RosettaNamedElement // attrName string (1..1)
class RosettaFeatureCall : RosettaPsiElement // expr.field
```

**Rationale**: Mirrors Rune DSL Xcore models (RosettaPackage.ecore) while fitting IntelliJ PSI model

#### 2.2 Threading Model
**Decision**: Use `ReadAction.compute` for PSI access, `WriteCommandAction` for modifications
**Rationale**: Constitution principle "Threading/PSI Safety" requires no EDT blocking

**Example**:
```kotlin
// Code completion provider
override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    ReadAction.compute<Unit, Throwable> {
        val element = parameters.position
        // Safe PSI access here
    }
}

// Quick fix action
override fun invoke(project: Project, editor: Editor, file: PsiFile) {
    WriteCommandAction.runWriteCommandAction(project) {
        // PSI modifications here
    }
}
```

#### 2.3 Stub Indexes
**Decision**: Implement stub indexes for types, functions, enums to support "Find Usages" and cross-file navigation
**Approach**:
- Extend `com.intellij.psi.stubs.StubIndexExtension`
- Key by fully-qualified name (e.g., "com.example.Foo" for type Foo in namespace com.example)
- Serialize minimal data (name, kind, cardinality) to avoid full parse on index lookup

**Rationale**: Performance goal (<300ms diagnostics) requires fast symbol resolution without parsing all files

#### 2.4 Smart/Dumb Mode
**Decision**: Gracefully degrade features during indexing (dumb mode)
**Approach**:
- Use `DumbService.isDumb(project)` to check mode
- Disable cross-file navigation/completion in dumb mode, show "Indexing..." status
- Enable file-local features (syntax highlighting, basic completion) always

**Rationale**: Constitution principle "PSI/VFS interactions respect smart/dumb mode"

**References**:
- IntelliJ PSI Cookbook: https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html
- Stub Indexes: https://plugins.jetbrains.com/docs/intellij/stub-indexes.html
- Threading: https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html

---

## 3. Code Generation Architecture

### Context
Rune DSL generates Java code from .rosetta models via:
- **Rosetta generators** (rune-lang/src/main/java/com/regnosys/rosetta/generator/)
- **Maven plugin** (rune-maven-plugin/src/main/java/com/regnosys/rosetta/maven/AbstractRuneGeneratorMojo.java)
- **Output**: Java classes (model POJOs, validators, builders)

### Decision: IntelliJ Build System Integration (Not LSP)
**Approach**:
1. **Build Process Integration**:
   - Implement `com.intellij.compiler.CompileTask` to invoke Rune generators before Java compilation
   - Use `com.intellij.openapi.compiler.CompilerManager` to register custom compilation step
   - Generate Java sources to `build/generated/sources/rune/` (follow Gradle conventions)
2. **Preview/Apply Flow** (P2 user story):
   - Show diff view (`com.intellij.diff.DiffManager`) of generated Java code before applying
   - Use `com.intellij.openapi.application.WriteAction` to modify VFS files atomically
   - Trigger re-index after generation to update PSI/caches

**Alternatives Considered**:
- **Gradle Plugin**: Create dev.mouli.rune-gradle-plugin; rejected as out-of-scope for initial plugin (can be future enhancement)
- **LSP Code Actions**: Rejected; IntelliJ native build integration provides better IDE experience
- **Manual Invocation**: Rejected; users expect automatic generation on build (like Lombok, Kotlin annotation processors)

**Trade-offs**:
- ✅ Seamless IDE experience (auto-generate on build, no manual steps)
- ❌ Complex build system integration (must support Maven, Gradle, IntelliJ's own build)
- ❌ Requires bundling Rune DSL generator JARs or downloading at runtime

**References**:
- IntelliJ Compiler API: https://plugins.jetbrains.com/docs/intellij/external-builder-api.html
- Diff View: https://plugins.jetbrains.com/docs/intellij/diff.html
- Rune Maven Plugin: finos/rune-dsl/rune-maven-plugin/src/main/java/com/regnosys/rosetta/maven/

---

## 4. VS Code Extension Parity Analysis

### Context
VS Code extension (rune-ide/vscode/) provides:
- **Language Server**: RosettaLanguageServerImpl with LSP features (completion, diagnostics, hover, etc.)
- **Syntax Highlighting**: TextMate grammar (rune-language-5.0.0.vsix)
- **Build Integration**: None (relies on external Maven/Gradle builds)

### Parity Checklist

| Feature                  | VS Code LSP       | IntelliJ Plugin Target | Priority |
|--------------------------|-------------------|------------------------|----------|
| Syntax Highlighting      | ✅ TextMate       | ✅ Lexer + highlighter | P0       |
| Code Completion          | ✅ LSP            | ✅ CompletionContributor | P1     |
| Diagnostics (errors)     | ✅ LSP            | ✅ Annotator/Inspections | P1     |
| Go to Definition         | ✅ LSP            | ✅ PsiReference        | P1       |
| Find Usages              | ✅ LSP            | ✅ FindUsagesProvider  | P1       |
| Hover Documentation      | ✅ LSP            | ✅ DocumentationProvider | P1     |
| Code Generation          | ❌ (manual build) | ✅ CompileTask + Preview | P2     |
| Refactoring (Rename)     | ❌                | ✅ (future enhancement) | P3       |
| Project Setup Wizard     | ❌                | ✅ ProjectTemplateFactory | P3    |

**Decision**: Target P0-P1 features for initial release; P2-P3 as post-launch enhancements

**Rationale**:
- P0-P1 features provide "authoring and navigation" (spec user story 1) parity with VS Code
- P2 features address "code generation" (spec user story 2) gap in VS Code extension
- P3 features exceed VS Code capabilities (IntelliJ-specific strengths)

**References**:
- VS Code Extension: finos/rune-dsl/rune-ide/vscode/src/extension.ts
- LSP Capabilities: finos/rune-dsl/rune-ide/src/main/java/com/regnosys/rosetta/ide/server/RosettaCapabilitiesContributor.java

---

## Open Questions

1. **Rune DSL Version Support**: Should plugin support multiple Rune DSL versions (e.g., per-project grammar versions)?
   - **Proposed Answer**: Initial release targets single grammar version (match latest rune-dsl main branch); multi-version support deferred to P3
2. **Java Interop**: Should plugin provide Kotlin DSL for Rune models (beyond just editing .rosetta files)?
   - **Proposed Answer**: No; focus on .rosetta editing; Kotlin DSL could be future extension point
3. **Error Recovery**: How to handle malformed .rosetta files gracefully?
   - **Proposed Answer**: Implement error PSI elements (`PsiErrorElement`) for partial parse recovery; show diagnostics inline (similar to VS Code LSP errors)

---

## Next Steps

1. **Phase 1: Design**
   - Create `data-model.md` with PSI hierarchy, generator interfaces, project structure entities
   - Define `contracts/` for Rune DSL generator invocation protocol (Java classpath-based or Maven/Gradle delegation)
   - Document `quickstart.md` for plugin users: install, create Rune project, validate, generate

2. **Prototype Spike**: Build minimal lexer/parser for "type Foo: attrName string (1..1)" construct to validate approach

3. **Constitution Re-Check**: Validate design against gates (Kotlin-first, Threading/PSI, Architecture, Testing/CI, Versioning)
