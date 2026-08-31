# study_suggestion_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the study-suggestion settings sheet.

## When it fires

The user closes the Profile › Preferences › Study suggestion sheet via its close affordances.

## Trigger source

`feature/preferences/study_suggestion/.../presentation/model/StudySuggestionUiEvent.kt` — `StudySuggestionUiEvent.OnDismiss`, emitted automatically.

## Parameters

None beyond the standard context.

## Notes

- The sheet applies changes immediately, so a dismissal after [study_suggestion_toggled](study_suggestion_toggled.md) or [study_suggestion_mode_changed](study_suggestion_mode_changed.md) is a completed configuration, not an abandonment.
