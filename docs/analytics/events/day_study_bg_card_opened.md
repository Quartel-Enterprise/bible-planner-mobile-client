# day_study_bg_card_opened

**Tier:** P2 | **Domain:** DayStudy

Captures the user opening a day study from the app-wide background generation card — the floating card that surfaces studies generating (or already finished) on a day that is not the one currently on screen. Measures how often users return to a backgrounded generation from the card versus letting it finish silently.

## When it fires

The user taps a job row (or its "Open" button) on the background generation card. This requests the study be opened and navigates to its day route.

## Trigger source

`feature/day_study/.../presentation/component/DayStudyBackgroundGenerationOverlay.kt` → `DayStudyBackgroundGenerationUiEvent.OnOpenClick`, auto-emitted by `DayStudyBackgroundGenerationViewModel` (base `TrackedViewModel`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Active reading plan of the opened day |
| `week_number` | int | `12` | 1-based week of the opened day |
| `day_number` | int | `3` | 1-based day within the week |
| `is_ready` | boolean | `true` | `true` when the study had finished generating (Done); `false` when it was still generating |

## Notes

- The card only surfaces backgrounded days (the day currently on screen is suppressed), so this event never overlaps with the foreground [day_study_card_clicked](day_study_card_clicked.md).
- A separate [destination_view](destination_view.md) for `day_study` also fires from the navigation that follows; this event measures the click-through, not the screen view.
