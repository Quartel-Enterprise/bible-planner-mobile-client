---
name: root-horizontal-safe-insets
description: App root consumes WindowInsets.safeDrawing horizontally so screens and Nav3 scenes never re-apply left/right safe areas; iOS navigationBars is bottom-only, so it cannot stand in for the Dynamic Island landscape insets.
metadata:
  type: project
---

AppRoot.kt pads RootAppNavDisplay with `WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)` (changed 2026-09-13; it was `navigationBars` before). On iOS, Compose Multiplatform maps `navigationBars` to the bottom safe area only and `systemBars`/`safeDrawing` to the full safe area, so the old root padding was a no-op on iOS: every Scaffold and NavigationRail re-applied the 59pt landscape safe area (double gap beside the rail) while split-pane scenes with no Scaffold (day study panel, verse selection) ran under the Dynamic Island.

**Why:** Nav3 scenes put two entries side by side in a Row. A per-screen Scaffold cannot know it owns only one side, and siblings never see each other's consumed insets. Consuming once at the root lets `Scaffold.onConsumedWindowInsetsChanged` and `windowInsetsPadding` subtract it automatically everywhere below.

**How to apply:** Never compute horizontal safe-area padding with `asPaddingValues()` (it ignores consumption; that was the chat sidebar width bug). Rely on `windowInsetsPadding` / Scaffold `contentWindowInsets`, which already exclude what the root consumed. Vertical insets (status bar, home indicator, IME) stay per-screen. Related: [[project_stable_window_insets_workaround]], [[project_dialog_sheet_insets]].
