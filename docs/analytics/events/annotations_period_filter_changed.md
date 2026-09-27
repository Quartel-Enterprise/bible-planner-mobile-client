# annotations_period_filter_changed

**Tier:** P2 | **Domain:** Verse annotations

Narrowing the list to what was marked recently.

## When it fires

The user picks a period in the date menu on the phone layout or in the Period section of the wide filter panel. Picking the selected period again clears it back to any date. Picking Custom opens a range picker, and this event fires only once a range is applied.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnPeriodSelected`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `period` | string | last_7_days | The period applied after the tap: `any`, `today`, `last_7_days`, `last_30_days` or `custom` |

## Notes

- Tracked manually because the applied period depends on the one selected before the tap.
- The period is measured against the last time the passage's highlight, bookmark or note changed.
