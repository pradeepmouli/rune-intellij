# Feature Specification: Rune DSL IntelliJ Plugin

**Feature Branch**: `001-rune-dsl-intellij`  
**Created**: 2025-11-05  
**Status**: Draft  
**Input**: User description: "Using the VS Code plugin https://github.com/finos/rune-dsl/tree/main/rune-ide as a reference, implement a rune-dsl (https://github.com/finos/rune-dsl/tree/main) plugin for IntelliJ. In addition to editor support, include tooling for code generation and related developer workflows."

## User Scenarios & Testing (mandatory)

### User Story 1 - Author and Navigate Rune DSL (Priority: P1)

Developers can open Rune DSL files and receive rich editing assistance including syntax highlighting, diagnostics, code completion, hover info, symbol navigation, and outline.

**Why this priority**: Editing feedback is the core value of an IDE plugin; without this, the plugin fails to deliver basic utility.

**Independent Test**: Open a sample Rune DSL project; verify highlighting, completion, diagnostics, and navigation without any generation tooling installed.

**Acceptance Scenarios**:

1. Given a valid Rune DSL file, When the user types within a construct, Then context-aware completion suggests valid keywords, identifiers, and structures.
2. Given a file with errors, When the user pauses typing, Then diagnostics appear inline with messages and positions; quick-fix placeholders are visible where applicable.
3. Given a symbol reference, When the user invokes "go to definition", Then the cursor navigates to the defining location in the project.
4. Given a Rune DSL file, When the user hovers recognized constructs, Then a documentation tooltip appears with summary and, when available, links to reference docs.

---

### User Story 2 - Generate Code from Rune DSL (Priority: P2)

Developers can trigger code generation from their Rune DSL sources and preview, diff, and apply generated artifacts into the workspace using a guided flow.

**Why this priority**: Generation is a primary outcome for DSLs; enabling developers to see and apply outputs accelerates adoption and feedback cycles.

**Independent Test**: With an existing Rune DSL sample, invoke generation, preview changes, and apply outputs into a sandbox directory without needing any additional features.

**Acceptance Scenarios**:

1. Given a Rune DSL project with a generation profile, When the user invokes "Generate from Rune", Then a preview UI shows changed/new files and allows accept/reject per file.
2. Given large projects, When generation runs, Then progress and cancellation are available and the IDE remains responsive.
3. Given generator configuration, When misconfigured or missing, Then the user sees a clear error with suggested remediation.

---

### User Story 3 - Project Setup, Validation, and Commands (Priority: P3)

Developers can initialize a Rune DSL project/module, validate configurations, and run common commands from the IDE (validate, generate, export), including basic UI affordances.

**Why this priority**: Reduces setup friction and consolidates frequently used actions; improves developer experience over ad-hoc terminals.

**Independent Test**: Create a new Rune DSL project using the IDE flow, run validation, and export artifacts without relying on other user stories.

**Acceptance Scenarios**:

1. Given the IDE without Rune DSL content, When the user selects "New Rune Project/Module", Then a guided flow scaffolds the minimal structure and configuration files.
2. Given a configured project, When the user runs "Validate", Then a report lists issues grouped by file with links to navigate to each.
3. Given an existing project, When the user runs "Export", Then a packaged artifact or report is produced in the configured output location.

---

### Edge Cases

- Extremely large files or projects should not freeze the IDE; background processing with progress and cancellation must be available.
- Invalid or mixed-version DSL files are flagged with actionable messages; version mismatch is clearly indicated.
- Generated outputs colliding with existing files prompt for overwrite/merge decisions; previews display diffs.
- Paths with spaces and non-ASCII characters are handled in generation and exports.
- Projects missing configuration files show targeted guidance to create or repair them.

## Requirements (mandatory)

### Functional Requirements

- **FR-001**: The system MUST provide syntax highlighting for Rune DSL constructs (keywords, types, declarations, references).
- **FR-002**: The system MUST provide context-aware code completion within valid positions of Rune DSL files.
- **FR-003**: The system MUST provide inline diagnostics for syntax/semantic errors with precise locations and messages.
- **FR-004**: The system MUST support navigation features: go to definition, find usages, and document outline for symbols.
- **FR-005**: The system MUST provide hover documentation for recognized constructs where reference text is available.
- **FR-006**: The system MUST allow users to trigger code generation from a Rune DSL project and preview outputs before applying.
- **FR-007**: The system MUST provide progress indication and cancellation for long-running generation tasks.
- **FR-008**: The system MUST surface configuration issues (missing/malformed profiles) with clear remediation guidance.
- **FR-009**: The system MUST support creating a new Rune DSL project/module via an IDE flow with minimal viable structure.
- **FR-010**: The system MUST support a validation command that produces a navigable report of issues grouped by file.
- **FR-011**: The system MUST support exporting generated artifacts or reports to a user-selected output location.
- **FR-012**: The system MUST keep the IDE responsive during background operations.
- **FR-013**: The system MUST operate cross-platform (Windows, macOS, Linux) for the primary editing and generation flows.
- **FR-014**: The system MUST allow configuration of generator profiles and selection at run time.
- **FR-015**: The system MUST maintain parity with the referenced extension’s user-visible behaviors for editing and generation flows (where applicable), without prescribing implementation.

### Key Entities (include if feature involves data)

- **DSL Document**: A source file written in Rune DSL; has grammar version, symbols, and diagnostics.
- **Grammar Version**: The declared or inferred version of the DSL grammar used to parse/validate documents.
- **Project Configuration**: User-provided settings that inform validation and generation (e.g., generator profiles, output paths).
- **Generator Profile**: A named set of generation settings selectable by the user to produce outputs.
- **Diagnostics Report**: A collection of issues produced by validation or generation phases, with locations and severities.
- **Generated Artifact**: An output file produced from DSL inputs via a selected generator profile.

## Success Criteria (mandatory)

### Measurable Outcomes

- **SC-001**: In typical projects, 95% of code completion suggestions appear within 200 ms of typing pause.
- **SC-002**: Diagnostics appear within 300 ms after the user stops typing on 90% of edits in typical files.
- **SC-003**: Generation of a representative sample project completes in under 30 seconds on a developer-class machine.
- **SC-004**: 90% of users can create a new Rune DSL project and successfully run validation without external documentation.
- **SC-005**: Zero IDE freezes during editing or generation under normal usage in a 1-hour session.
- **SC-006**: Users can preview and selectively apply at least 95% of generated file changes via the IDE flow.

## Assumptions

- The referenced VS Code extension’s user-visible behavior is the target for feature parity; implementation and technology stacks may differ.
- Rune DSL grammar and generation capabilities are available from the Rune project and can be consumed by the IDE plugin.
- Projects may define one or more generator profiles; when absent, the plugin provides guidance to create defaults.
