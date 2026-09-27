# annotations_filter_menu_dismissed

**Tier:** P2 | **Domain:** Verse annotations

The book or date filter menu closed.

## When it fires

The dropdown's `onDismissRequest` fires — tapping outside, pressing back, or the platform closing the native menu.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnFilterMenuDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Generic dismiss callback: treat volumes directionally, like [plan_overflow_dismissed](plan_overflow_dismissed.md).
