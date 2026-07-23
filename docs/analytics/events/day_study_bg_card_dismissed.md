# day_study_bg_card_dismissed

**Tier:** P2 | **Domain:** DayStudy

Captures the user dismissing the app-wide background generation card without opening the study. Distinguishes dismissing a single job row from dismissing all of them at once via the card header.

## When it fires

The user taps the "X" on a single job row, or the "X" in the multi-job header that clears every visible job. The underlying generation keeps running (and caching) regardless.

## Trigger source

`feature/day_study/.../presentation/component/DayStudyBackgroundGenerationOverlay.kt` → `DayStudyBackgroundGenerationUiEvent.OnDismissClick`, auto-emitted by `DayStudyBackgroundGenerationViewModel` (base `TrackedViewModel`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `count` | int | `1` | Number of jobs dismissed — `1` for a single row, `>1` for the "dismiss all" header action |

## Notes

- Dismissing only hides the card; it does not cancel generation, so a dismissed study can still be opened later from its day.
