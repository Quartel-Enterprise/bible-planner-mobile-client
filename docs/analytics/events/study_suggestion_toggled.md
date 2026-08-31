# study_suggestion_toggled

**Tier:** P1 | **Domain:** Settings

Captures the user turning the day-study suggestion on or off — the master switch for the celebration surface that appears when the day's last chapter is marked read.

## When it fires

The user flips the "Show the suggestion" switch in Profile › Preferences › Study suggestion, or taps "Don't show again" inside the day-reading-complete sheet.

## Trigger source

`feature/preferences/study_suggestion/.../presentation/model/StudySuggestionUiEvent.kt` — `StudySuggestionUiEvent.OnToggleClick` (source `settings`), and `feature/day_reading_complete/.../presentation/model/DayReadingCompleteUiEvent.kt` — `DayReadingCompleteUiEvent.OnNeverShowAgainClick` (source `day_reading_complete`, always `is_enabled = false`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | boolean | `false` | The value the toggle was set to |
| `source` | string | `settings` | Where the toggle happened: `settings` or `day_reading_complete` |

## Notes

- The `day_reading_complete` source is the opt-out path promised by the sheet's "Don't show again" link; a spike there means the sheet is felt as intrusive.
- Re-enabling is only possible from the settings sheet, so `is_enabled = true` always carries `source = settings`.
