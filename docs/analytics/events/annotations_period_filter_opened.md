# annotations_period_filter_opened

**Tier:** P2 | **Domain:** Verse annotations

The date filter menu was opened on the phone layout.

## When it fires

The user taps the Date chip in the filter bar.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnPeriodFilterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The wide layout lists the periods inline, so it never fires this event.
