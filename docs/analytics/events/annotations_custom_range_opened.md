# annotations_custom_range_opened

**Tier:** P2 | **Domain:** Verse annotations

The custom date range picker was opened, to narrow the list to what was marked between two dates.

## When it fires

The user picks Custom in the date menu (phone) or the Period section of the wide filter panel while no custom range is applied.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnPeriodSelected`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Ends in [annotations_period_filter_changed](annotations_period_filter_changed.md) with `period = custom` when a range is applied, or in [annotations_custom_range_dismissed](annotations_custom_range_dismissed.md).
