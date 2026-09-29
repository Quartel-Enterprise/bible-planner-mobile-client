---
name: project-feature-isolation
description: "Features never depend on features (moduleGraphAssert rule, Sep/2026, ported from TuneScout); shared domain goes to core, embedded feature UI goes through slot interfaces wired by core:navigation."
metadata:
  node_type: memory
  type: project
  originSessionId: 0ae9f3cd-71b2-49a0-8aa1-d753126492ca
  modified: 2026-09-26T15:30:30.146Z
---

Since branch `refactor/feature-isolation` (2026-09-26) `":feature:.* -X> :feature:.*"` is in the root `moduleGraphAssert` (production configs only; test-only feature deps allowed, e.g. `feature/day` androidHostTest → `feature/day_study` for store screenshots).

How the 19 old edges were removed:
- Shared domain/data moved to core: `core/day_study`, `core/in_app_update`, `core/preferences/{theme_selection,material_you,study_suggestion}`. Koin split: core keeps plain name (`dayStudyModule`), feature presentation gets `feature` prefix (`featureDayStudyModule`), same as `profileModule`/`featureProfileModule`.
- `Theme`/`ContrastType` moved from `ui.theme.model` to `core.model.theme` (core can't see ui); `LocalTheme` stays in ui:theme, which now `api`s core:model.
- Embedded feature UI: host declares a composable `fun interface` slot (`DayStudySectionSlot`, `DayCompletionBannerSlot`), `core/navigation/slot/Root*` objects implement it; `main` takes `MainTabEntries` for its tabs.
- Language-name strings + `Language.toStringResource()` moved to ui:component (contact_support/profile used app_language's public Res).

**Why:** user replicated TuneScout's isolation ([[project-module-graph-assert]], [[project-tunescout-ktlint-port]]).
**How to apply:** a new cross-feature need means moving code down to core/ui or adding a slot — never a rule exception. Documented in docs/architecture/module-structure.md#dependency-rules.
