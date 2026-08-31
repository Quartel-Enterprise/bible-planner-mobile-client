---
name: project_navigator_migration
description: "navigation is one injected Navigator (core/model) called from ViewModels; UiAction must never carry navigation, with two documented exceptions."
metadata: 
  node_type: memory
  type: project
  originSessionId: c8a4c1c8-0002-4d31-9f47-6268ef92e7d5
  modified: 2026-08-31T01:31:58.496Z
---

Navigation went from lambdas threaded through `toEntryProvider` to a single injected `Navigator` (`core/model`) that ViewModels call directly (PRs #388–#399, Aug/2026). `Navigator` sends `NavigationCommand`s over a buffered `Channel`; `RootAppNavDisplay` collects them into `BackStackController`, so the back stack is still mutated only from inside the composition.

**Why:** the old shape forced every screen to declare `NavigateTo…`/`NavigateBack` cases in its `UiAction` and a collector to translate them back into root lambdas — ~50 cases across 37 features. The old `NavigationEventBus` also used `MutableSharedFlow(replay = 1)` + a manual `reset()`, which dropped the second of two commands sent in quick succession.

**How to apply:** inject `Navigator` into the ViewModel. `UiAction` is only for effects the UI layer performs itself (snackbar, clipboard, share, scroll, focus). For a pure-UI back callback with no `UiEvent` (a dialog close button), `koinInject<Navigator>()` in the entry and pass `navigator::navigateBack` down — never reach into DI from leaf composables. Two things that look like navigation but are not, and stayed as `UiAction`: `MainScreenUiAction.NavigateToBottomRoute` (bottom-tab state, not the back stack) and `DayStudyUiAction`'s cases (callbacks into the day screen that hosts the card). Conventions live in [docs/architecture/navigation.md]. See [[feedback_worktree_remote_only]] for the PR-per-batch workflow used.
