# Quickstart: Rune DSL IntelliJ Plugin

**Audience**: Developers using the Rune DSL IntelliJ plugin to author and generate code from Rune DSL models  
**Prerequisites**: IntelliJ IDEA 2024.2+, JDK 21+

---

## Installation

### Option 1: JetBrains Marketplace (Recommended)
1. Open IntelliJ IDEA
2. Navigate to **Settings → Plugins → Marketplace**
3. Search for "Rune DSL"
4. Click **Install** and restart IDE

### Option 2: Manual Installation (Development Build)
1. Download `rune-intellij-plugin-X.Y.Z.zip` from GitHub releases
2. Open IntelliJ IDEA → **Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select downloaded ZIP file
4. Restart IDE

---

## Create a New Rune DSL Project

### Step 1: New Project Wizard
1. **File → New → Project**
2. Select **Rune DSL** from project types
3. Configure:
   - **Project Name**: my-rune-model
   - **Location**: ~/projects/my-rune-model
   - **Build System**: Maven or Gradle (recommended)
   - **Rune DSL Version**: 5.0.0 (latest)
4. Click **Create**

### Step 2: Project Structure
Plugin creates:
```text
my-rune-model/
├── src/
│   └── main/
│       └── rosetta/
│           └── com/
│               └── example/
│                   └── model.rosetta  # Sample .rosetta file
├── build.gradle.kts                   # Gradle config (or pom.xml for Maven)
└── .idea/
    └── rune-dsl-plugin.xml            # Plugin settings
```

---

## Author Rune DSL Models

### Step 3: Create a Type
1. Open `src/main/rosetta/com/example/model.rosetta`
2. Define a simple type:
   ```rosetta
   namespace com.example
   
   type Person:
       firstName string (1..1)
       lastName string (1..1)
       age int (0..1)
   ```

3. **Code Insights** (as you type):
   - ✅ **Syntax Highlighting**: Keywords (`type`, `namespace`), types (`string`, `int`)
   - ✅ **Code Completion**: Press `Ctrl+Space` after `age` to see type suggestions
   - ✅ **Diagnostics**: Red underlines for errors (e.g., duplicate attribute names)

### Step 4: Add a Function
```rosetta
func GetFullName:
    inputs:
        person Person (1..1)
    output:
        fullName string (1..1)
    
    set fullName:
        person -> firstName + " " + person -> lastName
```

### Step 5: Navigate Code
- **Go to Definition**: `Ctrl+Click` on `Person` in `GetFullName` inputs → jumps to `type Person`
- **Find Usages**: Right-click `Person` → **Find Usages** → shows all references
- **Structure View**: `Alt+7` → see tree of types, functions, attributes

---

## Validate Models

### Step 6: Run Validation
1. **Background Validation**: Plugin validates continuously (300ms after typing stops)
2. **Manual Trigger**: Right-click file → **Rune DSL → Validate**
3. **View Diagnostics**: Check **Problems** panel (`Alt+6`) for errors/warnings

### Example Error
```rosetta
type Employee:
    person Prsn (1..1)  // ❌ ERROR: Unresolved reference 'Prsn'
```
**Quick Fix**: Press `Alt+Enter` → **Create type 'Prsn'** or **Change to 'Person'**

---

## Generate Java Code

### Step 7: Configure Generator
1. **Settings → Rune DSL → Generators**
2. Enable **Default Java Generator**:
   - **Output Package**: `com.example.generated`
   - **Options**:
     - ✅ Generate Builders
     - ❌ Generate Validators (optional)
3. Click **Apply**

### Step 8: Preview Generation
1. Right-click `model.rosetta` → **Rune DSL → Generate Preview**
2. **Diff View** shows:
   - **Left**: Current generated code (if exists)
   - **Right**: New generated code
3. Review changes → Click **Accept All** or selectively apply

### Step 9: Build Project
1. **Build → Build Project** (`Ctrl+F9`)
2. Plugin generates Java sources to `build/generated/sources/rune/`:
   ```text
   build/generated/sources/rune/
   └── com/example/generated/
       ├── Person.java
       ├── PersonBuilder.java
       └── GetFullName.java
   ```
3. **IntelliJ Auto-Detects** generated sources → available in Java classpath

### Step 10: Use Generated Code
```kotlin
// In Kotlin/Java source file
import com.example.generated.Person
import com.example.generated.PersonBuilder

val person = PersonBuilder()
    .setFirstName("John")
    .setLastName("Doe")
    .setAge(30)
    .build()

println(GetFullName.execute(person)) // "John Doe"
```

---

## Performance Tips

### Incremental Generation
- Plugin only regenerates files whose `.rosetta` sources changed (checksum-based)
- **Manual Override**: Right-click project → **Rune DSL → Force Full Regeneration**

### Large Projects (1000+ .rosetta files)
1. **Exclude Test Models**: Settings → Rune DSL → Generators → Exclude Patterns: `src/test/**`
2. **Parallel Generation**: Enable in settings (uses `Runtime.availableProcessors()`)
3. **Smart Mode**: Disable cross-file navigation during indexing (plugin auto-detects dumb mode)

---

## Troubleshooting

### Issue: "Generator not found" error
**Cause**: Rune DSL generator JARs missing from classpath  
**Fix**:
1. For Gradle:
   ```kotlin
   dependencies {
       implementation("com.regnosys.rosetta:rune-lang:5.0.0")
       implementation("com.regnosys.rosetta:rune-maven-plugin:5.0.0")
   }
   ```
2. For Maven:
   ```xml
   <dependency>
       <groupId>com.regnosys.rosetta</groupId>
       <artifactId>rune-lang</artifactId>
       <version>5.0.0</version>
   </dependency>
   ```

### Issue: Syntax highlighting not working
**Cause**: File not recognized as .rosetta  
**Fix**: Right-click file → **Associate with File Type...** → Select **Rune DSL**

### Issue: Code completion shows no suggestions
**Cause**: Index still building  
**Fix**: Wait for indexing to complete (check status bar at bottom)

---

## Advanced Features (P3)

### Project Templates
- **File → New → Rune DSL Project from Template**
- Templates: "Trading Models", "Regulatory Reporting", "CDM"

### Refactoring
- **Rename Type/Function**: `Shift+F6` → updates all references
- **Extract Variable**: Select expression → `Ctrl+Alt+V`

### Integration with External Tools
- **CI/CD**: Use Gradle/Maven tasks for headless generation
  ```bash
  ./gradlew generateRuneJava
  ```

---

## Resources

- **Plugin Documentation**: https://github.com/pradeepmouli/rune-intellij/wiki
- **Rune DSL Spec**: https://github.com/finos/rune-dsl
- **Report Issues**: https://github.com/pradeepmouli/rune-intellij/issues
- **Community Chat**: FINOS Slack #rune-dsl channel

---

**Next Steps**: Explore [data-model.md](./data-model.md) for PSI structure and [contracts/generator-contract.md](./contracts/generator-contract.md) for extending generators
