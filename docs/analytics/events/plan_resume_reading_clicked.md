# plan_resume_reading_clicked

**Tier:** P2 | **Domain:** Books

Captures tapping "Resume reading" on the reading-plan hero when the user is behind. The tap opens the reader at the first unread chapter of the next day, so it is a distinct funnel signal from a day card tap that opens the Day screen.

## When it fires

User taps the primary "Resume reading" button of the behind-schedule hero card on the reading-plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnResumeReadingClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `week_number` | int | `12` | 1-based week of the day being resumed |
| `day_number` | int | `3` | 1-based day within the week |

## Notes

- Opens the reader directly; the Day screen is skipped, so [day_card_clicked](day_card_clicked.md) is not fired for this tap.
- Falls back to opening the Day screen when the first unread chapter cannot be resolved.
- Related: [plan_shortcut_used](plan_shortcut_used.md).
