# Tasks: Rune DSL IntelliJ Plugin

Feature: Rune DSL IntelliJ Plugin
Branch: 001-rune-dsl-intellij
Spec: specs/001-rune-dsl-intellij/spec.md
Plan: specs/001-rune-dsl-intellij/plan.md

---

## Phase 1 — Setup

- [ ] T001 Ensure IntelliJ Platform plugin configuration in app/build.gradle.kts (intellijPlatform 2024.2.4, pluginVerifier)
- [ ] T002 Add Rune DSL icons/resources placeholders in app/src/main/resources/icons/
- [ ] T003 Update plugin metadata in app/src/main/resources/META-INF/plugin.xml (id, name, vendor, sinceBuild/untilBuild)
- [x] T004 Create package com/github/pmouli/rune in app/src/main/kotlin/

## Phase 2 — Foundational (blocking prerequisites)

- [x] T005 Create language definition in app/src/main/kotlin/com/github/pmouli/rune/RosettaLanguage.kt
- [x] T006 Create file type registration in app/src/main/kotlin/com/github/pmouli/rune/RosettaFileType.kt
- [x] T007 Register language + file type EPs in app/src/main/resources/META-INF/plugin.xml
- [x] T008 Create lexer definition (.flex) in app/src/main/kotlin/com/github/pmouli/rune/lexer/RosettaLexer.flex
- [x] T009 Create lexer adapter in app/src/main/kotlin/com/github/pmouli/rune/lexer/RosettaLexerAdapter.kt
- [x] T010 Create parser grammar (.bnf) in app/src/main/kotlin/com/github/pmouli/rune/parser/Rosetta.bnf
- [x] T011 Create ParserDefinition in app/src/main/kotlin/com/github/pmouli/rune/RosettaParserDefinition.kt
- [x] T012 Create PSI base interfaces in app/src/main/kotlin/com/github/pmouli/rune/psi/RosettaPsiElement.kt
- [x] T013 Create PSI named element in app/src/main/kotlin/com/github/pmouli/rune/psi/RosettaNamedElement.kt
- [x] T014 Create PSI element factory in app/src/main/kotlin/com/github/pmouli/rune/psi/RosettaPsiElementFactory.kt
- [x] T015 Configure syntax highlighter keys in app/src/main/kotlin/com/github/pmouli/rune/highlight/RosettaSyntaxHighlighter.kt
- [x] T016 Register highlighter + color settings EPs in app/src/main/resources/META-INF/plugin.xml

---

## Phase 3 — User Story 1 (P1): Author and Navigate Rune DSL

Goal: Editing assistance (highlighting, completion, diagnostics, navigation, hover, outline).
Independent Test Criteria: Open sample .rosetta files and verify highlighting, completion, diagnostics, navigation without any generator configured.

- [ ] T017 [P] [US1] Implement token types and syntax highlighter in app/src/main/kotlin/com/github/pmouli/rune/highlight/RosettaSyntaxHighlighter.kt
- [ ] T018 [P] [US1] Implement color settings page in app/src/main/kotlin/com/github/pmouli/rune/highlight/RosettaColorSettingsPage.kt
- [ ] T019 [P] [US1] Implement PSI for root elements (types, functions, enums) in app/src/main/kotlin/com/github/pmouli/rune/psi/RosettaDataType.kt
- [ ] T020 [P] [US1] Implement PSI for attributes and expressions in app/src/main/kotlin/com/github/pmouli/rune/psi/RosettaAttribute.kt
- [ ] T021 [US1] Implement completion contributor in app/src/main/kotlin/com/github/pmouli/rune/completion/RosettaCompletionContributor.kt
- [ ] T022 [US1] Implement annotator (diagnostics) in app/src/main/kotlin/com/github/pmouli/rune/annotator/RosettaAnnotator.kt
- [ ] T023 [US1] Implement reference contributor (go to definition) in app/src/main/kotlin/com/github/pmouli/rune/reference/RosettaReferenceContributor.kt
- [ ] T024 [US1] Implement documentation provider in app/src/main/kotlin/com/github/pmouli/rune/doc/RosettaDocumentationProvider.kt
- [ ] T025 [US1] Implement structure view in app/src/main/kotlin/com/github/pmouli/rune/structure/RosettaStructureViewFactory.kt
- [ ] T026 [US1] Register all US1 extension points in app/src/main/resources/META-INF/plugin.xml

---

## Phase 4 — User Story 2 (P2): Generate Code from Rune DSL

Goal: Trigger generation, show diff preview, apply outputs with progress/cancel.
Independent Test Criteria: With an existing Rune sample, invoke generation and preview/apply into a sandbox folder.

- [ ] T027 [P] [US2] Define generator contract in app/src/main/kotlin/com/github/pmouli/rune/generation/RosettaGeneratorContract.kt
- [ ] T028 [P] [US2] Implement Rune Java adapter in app/src/main/kotlin/com/github/pmouli/rune/generation/RuneJavaGeneratorAdapter.kt
- [ ] T029 [P] [US2] Implement compile task in app/src/main/kotlin/com/github/pmouli/rune/generation/RosettaGenerationCompileTask.kt
- [ ] T030 [US2] Create Generate Preview action in app/src/main/kotlin/com/github/pmouli/rune/actions/GeneratePreviewAction.kt
- [ ] T031 [US2] Implement diff preview flow using DiffManager in app/src/main/kotlin/com/github/pmouli/rune/generation/PreviewService.kt
- [ ] T032 [US2] Add project settings configurable in app/src/main/kotlin/com/github/pmouli/rune/settings/RuneSettingsConfigurable.kt
- [ ] T033 [US2] Persist project configuration in app/src/main/kotlin/com/github/pmouli/rune/settings/RuneProjectConfiguration.kt
- [ ] T034 [US2] Register generator actions/services in app/src/main/resources/META-INF/plugin.xml

---

## Phase 5 — User Story 3 (P3): Project Setup, Validation, and Commands

Goal: Initialize Rune project/module, validate, and export from IDE.
Independent Test Criteria: Create a new Rune project via IDE flow, run validation, and export artifacts without relying on other stories.

- [ ] T035 [P] [US3] Add project/module wizard (template) in app/src/main/kotlin/com/github/pmouli/rune/project/RuneProjectTemplateFactory.kt
- [ ] T036 [US3] Implement Validate action producing navigable report in app/src/main/kotlin/com/github/pmouli/rune/actions/ValidateAction.kt
- [ ] T037 [US3] Implement Export action in app/src/main/kotlin/com/github/pmouli/rune/actions/ExportAction.kt
- [ ] T038 [US3] Register project wizard and actions in app/src/main/resources/META-INF/plugin.xml

---

## Final Phase — Polish & Cross-Cutting

- [ ] T039 [P] Add dumb-mode guards around cross-file features in app/src/main/kotlin/com/github/pmouli/rune/**
- [ ] T040 [P] Add ReadAction/WriteAction wrappers and progress/cancel to background tasks in app/src/main/kotlin/com/github/pmouli/rune/**
- [ ] T041 Update README.md with build/run/test instructions and link to specs/001-rune-dsl-intellij/quickstart.md
- [ ] T042 Ensure ktlint/detekt pass and CI green (no new warnings)
- [ ] T043 Run Plugin Verifier for target IDEs (242/243) and fix issues

---

## Dependencies (Story Order)

- Foundational (Phase 2) → enables US1 and US2 in parallel
- US1 (Editing) ↔ US2 (Generation): can proceed in parallel after foundational; US2 uses VFS and generator jars independent of PSI
- US3 (Project setup/validation/commands) can start after foundational; validation benefits from US1 diagnostics but is not strictly blocked

Graph:
- Phase 2 → { US1, US2, US3 }

---

## Parallel Execution Examples

- US1: T017, T018, T019, T020 in parallel (different files, no order coupling)
- US2: T027, T028, T029 in parallel (contract, adapter, compile task)
- Final: T039, T040 in parallel (cross-cutting guards)

---

## Implementation Strategy (MVP-first)

- MVP = Complete US1 (P1): lexer/parser for core constructs, PSI, completion, diagnostics, navigation, hover, outline
- Next: US2 (P2) generation preview/apply with default Java generator
- Then: US3 (P3) project wizard, validation report, export

---

## Counts & Validation

- Total Tasks: 43
- Per User Story: US1 = 10, US2 = 8, US3 = 4 (rest are setup/foundational/polish)
- Parallelizable: [P] on 9 tasks (see markings)
- Independent Test Criteria:
  - US1: Open sample project; verify editing features without generators
  - US2: Use sample; invoke Generate Preview; apply to sandbox
  - US3: New project via wizard; run Validate and Export
- Format Validation: All tasks follow `- [ ] T### [P]? [USn]? Description with file path` format
