---
name: project_root_nav_viewmodel_home
description: "Where to put a ViewModel/Koin module for a core/navigation root-level concern (it can't own one)"
metadata: 
  node_type: memory
  type: project
  originSessionId: a25355d5-7174-4809-b135-09a036076636
  modified: 2026-07-24T01:13:36.745Z
---

`core/navigation` (RootAppNavDisplay) is DOWNSTREAM of `core/provider/koin` — it `implementation(projects.core.provider.koin)`, and provider/koin's `CommonKoinUtils` aggregates every feature/core Koin module. So `core/navigation` CANNOT register its own Koin module (circular dep), and there is no `navigationModule`.

**How to give a root-level composable (RootAppNavDisplay) a ViewModel:** put the ViewModel + use cases + repository + Koin registration in a feature module that is ALREADY in the DI graph and that `core/navigation` already depends on. For the day/study split pane I used `feature/day_study` (its `dayStudyModule` is in CommonKoinUtils; navigation already depends on `projects.feature.dayStudy`). Then `RootAppNavDisplay` does `koinViewModel<DayStudyPanelViewModel>()` (add `libs.koinComposeViewModel` to navigation's build).

`App.kt` already hosts `koinViewModel<AppViewModel>()` at the same level as `RootAppNavDisplay`, so a ViewModelStoreOwner exists at that root — `koinViewModel` there is safe and app-scoped (single instance for app lifetime).

**Persisted split-pane ratio** lives here: `DayStudyPanelRatio` (DEFAULT/MIN/MAX bounds object, shared with the scene), `DayStudyPanelRatioRepository` (DataStore floatPreferencesKey `day_study_panel_reading_fraction`), Observe/Set use cases (clamp + default), `DayStudyPanelViewModel`. The draggable divider lives in `core/navigation` `DayStudyPanelScene`. Related: [[project_navigation3_migration]].
