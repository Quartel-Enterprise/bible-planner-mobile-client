# study_suggestion_mode_changed

**Tier:** P2 | **Domain:** Settings

Captures which presentation the user prefers for the day-study suggestion: the full sheet or the discreet banner.

## When it fires

The user taps one of the "How it appears" cards in Profile › Preferences › Study suggestion while the suggestion is enabled.

## Trigger source

`feature/preferences/study_suggestion/.../presentation/model/StudySuggestionUiEvent.kt` — `StudySuggestionUiEvent.OnModeClick`, emitted automatically.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `banner` | The chosen presentation: `dialog` or `banner` |

## Notes

- Fires on every card tap, including re-selecting the already active mode.
- Tapping a card while the suggestion is disabled fires [study_suggestion_mode_blocked_clicked](study_suggestion_mode_blocked_clicked.md) instead.
