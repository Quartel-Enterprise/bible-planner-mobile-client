---
name: dialog-sheet-insets
description: Nav-bar insets are 0 inside nested Dialog windows on Android; ResponsiveDialogSheet fixes via LocalNavigationBarInsets from App root + skipPartiallyExpanded=true
metadata: 
  node_type: memory
  type: project
  originSessionId: 54769dae-ae47-4eec-b8cc-700ac42478a2
---

On Android, `WindowInsets.navigationBars` / `navigationBarsPadding()` resolve to 0 when read inside the nested Dialog windows created by Nav3 `DialogSceneStrategy` + Material3 `ModalBottomSheet` (CMP limitation: `usePlatformInsets` not supported on Android). Two-part fix in `ResponsiveDialogSheet`:

1. `LocalNavigationBarInsets` (ui/utils) is provided in `App.kt` root — capturing the live `WindowInsets.navigationBars` object from the Activity window — and consumed via `windowInsetsPadding(LocalNavigationBarInsets.current)` in `CloseableContent`.
2. `skipPartiallyExpanded = true` is hardcoded in the sheet state: in the partially-expanded state the sheet content overflows past the screen bottom edge, so no amount of padding prevents the last row from being cut by the nav bar.

**Why:** padding-only fixes look correct but do nothing in partial expansion; verified empirically on emulator (screenshots before/after).

**How to apply:** any new dialog/sheet content inside a Dialog window must use [[dialog-sheet-insets]] `LocalNavigationBarInsets`, never `navigationBarsPadding()` directly; don't reintroduce a `skipPartiallyExpanded` parameter.
