# day_study_dismissed

**Tier:** P2 | **Domain:** DayStudy

Captures the AI study sheet being dismissed. Paired with [day_study_opened](day_study_opened.md), it lets analysis distinguish a quick bounce from real reading time.

## When it fires

The user closes the day study bottom sheet.

## Trigger source

`feature/day_study/.../presentation/model/DayStudyUiEvent.kt` — `DayStudyUiEvent.OnStudyDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- This event was previously classified as UI bookkeeping (`NotTracked`); it was reclassified because sheet dismissal, combined with [day_study_opened](day_study_opened.md) timing, is a genuine engagement signal for the AI feature.
