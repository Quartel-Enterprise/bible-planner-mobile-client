# annotations_custom_range_dismissed

**Tier:** P2 | **Domain:** Verse annotations

Backing out of the custom date range picker without applying a range.

## When it fires

The user taps Cancel in the range picker or dismisses it.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnCustomRangeDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The period that was applied before the picker opened stays in place.
