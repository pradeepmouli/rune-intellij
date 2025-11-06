# Rune IntelliJ Plugin

A minimal IntelliJ Platform plugin built with Kotlin, following best practices for threading, PSI/VFS safety, and clean architecture.

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
1. Go to **Tools → Hello Rune**
2. You should see a dialog with "Hello from Rune IntelliJ Plugin!"

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
│   │   │   │       └── actions/
│   │   │   │           └── HelloAction.kt
│   │   │   └── resources/
│   │   │       └── META-INF/
│   │   │           └── plugin.xml
│   │   └── test/
│   │       └── kotlin/
│   │           └── com/github/pmouli/rune/
│   │               └── actions/
│   │                   └── HelloActionTest.kt
│   └── build.gradle.kts
├── .editorconfig
├── detekt.yml
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

## Configuration

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
