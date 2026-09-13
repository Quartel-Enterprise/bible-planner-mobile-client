---
name: project-stable-window-insets-workaround
description: "WindowInsets.asStable() (ui/utils) works around the iOS \"visitSubtreeIf called on an unattached node\" crash on CMP 1.12.0; reported as CMP-10789, remove once CMP ships the androidx fix."
metadata: 
  node_type: memory
  type: project
  originSessionId: a2567adc-a6c2-4740-bec7-6338f82e78d2
  modified: 2026-09-13T22:50:19.385Z
---

On skiko (iOS/desktop), `WindowInsets.systemBars`/`navigationBars` (and M3 defaults built on them: Scaffold contentWindowInsets, TopAppBar/BottomAppBar windowInsets) return a new anonymous WindowInsets without equals on every recomposition. When Nav3 moves an entry's movableContent between scenes (e.g. reader ↔ VerseSelectionScene) and it recomposes while detached, the insets modifier updates on an unattached node and crashes (Crashlytics iOS issue d4e082ce, 2.8.0–2.8.4). Upstream androidx fix c9f28569 (b/447028569, 2026-09-11) is not in CMP 1.12.0 / 1.13.0-alpha01.

Workaround in PR #437 (2026-09-13): `WindowInsets.asStable()` in ui/utils snapshots values into value-equal `WindowInsets(Int…)`, applied to the reader screens/bars and to DayScreen/DayStudyScreen (moved by DayStudyPanelScene). Reported to JetBrains as https://youtrack.jetbrains.com/issue/CMP-10789.

**Why:** screens moved between Nav3 scenes must not feed unstable insets to inset modifiers on iOS.
**How to apply:** use `asStable()` for insets passed to windowInsetsPadding/Scaffold/TopAppBar/BottomAppBar in any screen that participates in a custom Scene. When bumping CMP, check CMP-10789 / the androidx fix; if included, drop the helper. Related: [[project-navigation3-migration]].
