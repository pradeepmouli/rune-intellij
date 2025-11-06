# Rune IntelliJ Plugin

IntelliJ IDEA plugin for the Rune DSL (Domain Specific Language), providing comprehensive editing support, code generation, project management, and validation tools.

Based on the [Rune DSL project](https://github.com/finos/rune-dsl) by FINOS, this plugin brings rich IDE features to Rune developers.

## Features

### 🎨 Rich Editing Support (User Story 1)
- **Syntax Highlighting**: Color-coded syntax for types, enums, functions, and more
- **Code Completion**: Context-aware completion for keywords, identifiers, and structures
- **Diagnostics**: Inline error and warning messages with quick-fix suggestions
- **Navigation**: Go-to-definition and find usages for symbols
- **Documentation**: Hover tooltips with symbol documentation
- **Structure View**: Outline view showing project structure

### ⚙️ Code Generation (User Story 2)
- **Generate from Rune**: Convert .rosetta files to Java code
- **Diff Preview**: Review changes before applying
- **Progress & Cancellation**: Background generation with progress tracking
- **Configurable**: Project-level settings for output directories and packages

### 📦 Project Management (User Story 3)
- **Project Wizard**: Create new Rune DSL projects with standard structure
- **Validation**: Scan and validate all .rosetta files with navigable reports
- **Export**: Export projects as ZIP, directory, or Maven project

## Quick Start

See the [Quick Start Guide](specs/001-rune-dsl-intellij/quickstart.md) for detailed setup and usage instructions.

## Prerequisites

- **JDK 21** (required for IntelliJ Platform 2024.2+)
- **Gradle** (wrapper included)

## Build & Run

### Build the plugin

```bash
./gradlew build
```

### Run in IntelliJ IDEA sandbox

```bash
./gradlew runIde
```

Once the sandbox IDE opens:
1. Create a new Rune DSL project using **File → New → Project → Rune DSL**
2. Edit the sample .rosetta file in `src/main/rosetta/`
3. Use **Tools → Validate Rune DSL Files** to check for errors
4. Right-click a .rosetta file and select **Generate Code Preview** to generate Java code
5. Use **Tools → Export Rune DSL Artifacts** to export your project

### Run tests

```bash
./gradlew test
```

### Run code quality checks

```bash
# Format check with ktlint
./gradlew ktlintCheck

# Auto-format with ktlint
./gradlew ktlintFormat

# Static analysis with detekt
./gradlew detekt
```

### Run IntelliJ Plugin Verifier

Verifies plugin compatibility with target IntelliJ Platform builds:

```bash
./gradlew runPluginVerifier
```

## Project Structure

```
rune-intellij/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/github/pmouli/rune/
│   │   │   │       ├── actions/           # IDE actions
│   │   │   │       ├── annotator/         # Error/warning annotations
│   │   │   │       ├── completion/        # Code completion
│   │   │   │       ├── doc/               # Documentation provider
│   │   │   │       ├── generation/        # Code generation
│   │   │   │       ├── highlight/         # Syntax highlighting
│   │   │   │       ├── lexer/             # Lexical analysis
│   │   │   │       ├── parser/            # Grammar/parsing
│   │   │   │       ├── project/           # Project templates
│   │   │   │       ├── psi/               # PSI elements
│   │   │   │       ├── reference/         # Cross-references
│   │   │   │       ├── settings/          # Configuration
│   │   │   │       ├── structure/         # Structure view
│   │   │   │       ├── RosettaFileType.kt
│   │   │   │       ├── RosettaLanguage.kt
│   │   │   │       └── RosettaParserDefinition.kt
│   │   │   └── resources/
│   │   │       └── META-INF/
│   │   │           └── plugin.xml
│   │   └── test/
│   │       └── kotlin/
│   └── build.gradle.kts
├── specs/
│   └── 001-rune-dsl-intellij/
│       ├── spec.md              # Feature specification
│       ├── plan.md              # Implementation plan
│       ├── tasks.md             # Task breakdown
│       └── quickstart.md        # Quick start guide
├── .editorconfig
├── detekt.yml
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

## Usage

### Creating a Rune DSL Project

1. **File → New → Project**
2. Select **Rune DSL** from the project types
3. Configure project settings and click **Create**
4. The wizard creates:
   - `src/main/rosetta/` - Source files
   - `src/generated/java/` - Generated code
   - `sample.rosetta` - Example file
   - `README.md` - Project documentation

### Editing Rune DSL Files

- Open any `.rosetta` file for syntax highlighting and code completion
- Hover over symbols for documentation
- Use Ctrl+Click (Cmd+Click on Mac) to navigate to definitions
- View errors and warnings inline

### Generating Code

1. Right-click a `.rosetta` file in the Project view
2. Select **Generate Code Preview**
3. Review the diff showing generated vs. existing code
4. Click **Apply** to write generated files to your project

### Validating Your Project

1. **Tools → Validate Rune DSL Files**
2. View validation report in the **Rune Validation** tool window
3. Click on issues to navigate to problem locations

### Exporting

1. **Tools → Export Rune DSL Artifacts**
2. Choose format: ZIP Archive, Directory, or Maven Project
3. Select destination folder
4. Review exported files

## Architecture

This plugin follows IntelliJ Platform best practices:
- **Threading**: All read/write operations use appropriate actions
- **PSI/VFS Safety**: Proper indexing and file access patterns
- **Background Tasks**: Long operations run with progress and cancellation support
- **Dumb Mode**: Features gracefully degrade during indexing

## Development Workflow

1. Make changes to Kotlin sources
2. Run `./gradlew ktlintFormat` to format code
3. Run `./gradlew detekt` to check for issues
4. Run `./gradlew test` to verify tests pass
5. Run `./gradlew runIde` to test in sandbox
6. Commit changes

## Documentation

- **[Feature Specification](specs/001-rune-dsl-intellij/spec.md)** - Detailed feature requirements
- **[Implementation Plan](specs/001-rune-dsl-intellij/plan.md)** - Development roadmap
- **[Task Breakdown](specs/001-rune-dsl-intellij/tasks.md)** - Granular task list
- **[Quick Start Guide](specs/001-rune-dsl-intellij/quickstart.md)** - Get started quickly

## Related Projects

- **[Rune DSL](https://github.com/finos/rune-dsl)** - Core Rune DSL project by FINOS
- **[Rune VS Code Extension](https://github.com/finos/rune-dsl/tree/main/rune-ide)** - Reference implementation

## Configuration

Access plugin settings via **File → Settings → Tools → Rune DSL**:
- **Output Directory**: Where generated Java files are written
- **Output Package**: Base package for generated classes
- **Generator Options**: Additional configuration for code generation

## Rune DSL Syntax

Basic constructs supported:

```rosetta
namespace "com.example.model"
version "1.0.0"

// Define a data type
type Person:
    firstName string (1..1)
    lastName string (1..1)
    age int (0..1)

// Define an enumeration
enum Status:
    ACTIVE
    INACTIVE

// Define a function
func validatePerson:
    inputs: person Person (1..1)
    output: result boolean (1..1)
```

## Architecture

- **Plugin ID**: `com.github.pmouli.rune-intellij`
- **IntelliJ Platform**: 2024.2.4 (Community Edition)
- **Compatibility**: builds 242–243.*
- **Kotlin**: 2.0.21
- **Java Toolchain**: 21

## Development Workflow

1. Make changes to Kotlin sources
2. Run `./gradlew ktlintFormat` to format code
3. Run `./gradlew detekt` to check for issues
4. Run `./gradlew test` to verify tests pass
5. Run `./gradlew runIde` to test in sandbox
6. Commit changes

## CI/CD

GitHub Actions workflow (`.github/workflows/build.yml`) runs on every push and PR:
- ✅ ktlint format check
- ✅ detekt static analysis
- ✅ build
- ✅ tests
- ✅ IntelliJ Plugin Verifier

## Constitution

This project follows the [Rune IntelliJ Plugin Constitution](.specify/memory/constitution.md), which defines:
- Kotlin-first development
- IntelliJ threading and PSI/VFS safety
- Clear plugin architecture
- Testing and static analysis requirements
- Versioning and compatibility policy

## Resources

- [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Kotlin for IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/using-kotlin.html)

## License

MIT
