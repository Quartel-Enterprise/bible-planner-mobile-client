---
name: project_sheet_scene_strategy
description: PR #551 (Oct/26) — sheet routes use getSheetPane()+SheetSceneStrategy (OverlayScene.onRemove) so they animate out; TuneScout's BottomSheetSceneStrategy lacks onRemove and wouldn't fix it.
metadata:
  type: project
---
Sheet routes animate out via `getSheetPane()` metadata + `SheetSceneStrategy` in core/navigation: `OverlayScene.onRemove()` waits for the exit animation the content registers (`SheetExitAnimationEffect`), and `rememberSheetCloseGuard()` stops a double tap on close from popping the screen underneath (a race that existed with DialogSceneStrategy too: both taps land before the first pop is processed).

**Why:** DialogSceneStrategy's scene leaves composition the instant its entry pops, so X / ViewModel `navigateBack()` closes vanished. TuneScout's `BottomSheetSceneStrategy` owns the ModalBottomSheet but has no `onRemove`, and routing swipe/scrim to NavDisplay's onBack would skip the ViewModels' dismiss analytics — so the sheet stays in the entry content here.

**How to apply:** new sheet routes use `getSheetPane()`; a hand-rolled ModalBottomSheet must register the effect + guard (docs/architecture/navigation.md). Never "hide then delay then back". Related: [[project_dialog_sheet_insets]], [[project_navigation3_migration]].
