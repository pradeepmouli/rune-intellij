<!--
Sync Impact Report
- Version change: N/A (template) → 1.0.0
- Modified principles:
	- [PRINCIPLE_1_NAME] → Kotlin‑First & Idiomatic Code
	- [PRINCIPLE_2_NAME] → IntelliJ Threading, PSI & VFS Safety
	- [PRINCIPLE_3_NAME] → Clear Plugin Architecture & Extension Points
	- [PRINCIPLE_4_NAME] → Testing, Static Analysis & CI
	- [PRINCIPLE_5_NAME] → Versioning, Compatibility & Distribution
- Added sections:
	- [SECTION_2_NAME] → Kotlin/IntelliJ Technical Constraints
	- [SECTION_3_NAME] → Development Workflow & Quality Gates
- Removed sections: None
- Templates requiring updates:
	- .specify/templates/plan-template.md ✅ updated (Constitution Check gates aligned)
	- .specify/templates/spec-template.md ⚠ pending (kept generic; no conflicting guidance)
	- .specify/templates/tasks-template.md ⚠ pending (kept generic; no conflicting guidance)
- Follow-up TODOs:
  - Compatibility matrix and CI Plugin Verifier target IDE builds deferred to implementation phase
  - IntelliJ Platform test framework integration deferred to implementation phase
  - Coverage threshold decision deferred to implementation phase (consider 70–80% for Kotlin code if required)
--># Rune IntelliJ Plugin Constitution

## Core Principles

### Kotlin‑First & Idiomatic Code

All production code MUST be written in Kotlin using idiomatic constructs:
- Prefer immutable data structures, data classes, sealed classes, and extension functions.
- Use Kotlin coroutines for background work; avoid blocking calls on the UI thread.
- Enforce null‑safety and explicit types where it clarifies contracts; minimize platform types.
- Adopt Kotlin DSL for Gradle builds and keep configuration in versioned files.
Rationale: Kotlin is the first‑class language for IntelliJ plugins and improves safety, clarity,
and developer velocity.

### IntelliJ Threading, PSI & VFS Safety

Code MUST respect IntelliJ Platform threading, indexing, and read/write rules:
- Never block the Event Dispatch Thread (EDT); long operations run with progress and cancellation.
- Use readAction/writeAction, DumbAware, and modality as appropriate; honor smart/dumb mode.
- Interact with PSI/VFS under correct access and on proper threads; batch changes via write actions.
- Use background tasks, coroutines with ModalityState, and ProgressManager for responsiveness.
Rationale: Correct threading prevents freezes and data corruption and is required for marketplace
acceptance.

### Clear Plugin Architecture & Extension Points

Structure plugin features using stable IntelliJ concepts:
- Register actions and extension points in plugin.xml; prefer services over deprecated components.
- Keep UI logic (actions, tool windows) separate from domain/services to enable testing.
- Use service lifecycles (application/project) intentionally; avoid global state and static singletons.
- Avoid deprecated APIs; guard optional platform features and handle absence gracefully.
Rationale: A clean architecture reduces coupling, eases upgrades, and improves testability.

### Testing, Static Analysis & CI

Quality gates MUST run on every change:
- Unit tests with JUnit 5; platform tests using IntelliJ test framework where behavior depends on IDE.
- Static analysis with ktlint (format) and detekt (rules); no "warning debt" in main branch.
- Run JetBrains Plugin Verifier against supported IDE builds; failures block release.
- Keep a fast plugin sandbox run configuration for manual verification.
Rationale: Prevent regressions, maintain code quality, and ensure marketplace compatibility.

### Versioning, Compatibility & Distribution

Maintain a clear compatibility contract:
- Semantic versioning for the plugin; changelog tracks user‑visible changes and breaking behavior.
- Explicit sinceBuild/untilBuild policy; target a supported baseline and test across a small matrix.
- Publish metadata complete: plugin description, vendor, links, privacy/security notes where relevant.
- Sign and verify distributions as required by marketplace policies.
Rationale: Predictable upgrades reduce user friction and support costs.

## Kotlin/IntelliJ Technical Constraints

- Use Gradle with the Gradle IntelliJ Plugin and Kotlin JVM.
- Target the current supported IntelliJ Platform baseline; set Java language level to the IDE runtime.
- Keep plugin.xml minimal and declarative; prefer programmatic registration only when necessary.
- Avoid long‑running work in constructors; use services and background tasks.
- Document extension points, settings, and user‑facing actions.

## Development Workflow & Quality Gates

1. Design change captured in issue/PR with scope and user impact.
2. Write/update tests first for user‑visible behavior where feasible.
3. Implement in Kotlin following threading/PSI rules; verify in sandbox.
4. CI MUST pass: build, tests, ktlint, detekt, and Plugin Verifier for target IDEs.
5. Review requires explicit checklist acknowledgment of this constitution.
6. Release bumps version and updates changelog; maintain compatibility matrix.

## Governance

- This constitution supersedes ad‑hoc practices for the plugin codebase.
- Amendments require PR review and rationale; include migration notes when changing principles.
- Versioning policy for this document: semantic (MAJOR for breaking governance changes,
	MINOR for new principles/sections, PATCH for clarifications).
- Compliance reviews happen at PR time and before releases; violations must be justified or fixed.

**Version**: 1.0.0 | **Ratified**: 2025-11-05 | **Last Amended**: 2025-11-05
