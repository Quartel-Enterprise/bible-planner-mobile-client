# study_suggestion_mode_blocked_clicked

**Tier:** P2 | **Domain:** Settings

Captures a tap on a "How it appears" card while the suggestion itself is turned off — the cards are dimmed and only answer with a snackbar explaining how to unlock them.

## When it fires

The user taps either presentation card in Profile › Preferences › Study suggestion while the "Show the suggestion" switch is off.

## Trigger source

`feature/preferences/study_suggestion/.../presentation/model/StudySuggestionUiEvent.kt` — `StudySuggestionUiEvent.OnBlockedModeClick`, emitted automatically.

## Parameters

None beyond the standard context.

## Notes

- Pairs with [study_suggestion_mode_changed](study_suggestion_mode_changed.md), which fires for the same tap when the suggestion is enabled.
- Frequent hits suggest the disabled treatment reads as tappable — a UX signal, not a funnel one.
