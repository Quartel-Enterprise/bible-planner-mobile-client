# sync_toggle_blocked_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping a sync toggle that is disabled/blocked (e.g. because they are logged out). Shows how often signed-out users attempt to enable sync, a signal for the login-nudge funnel.

## When it fires

User taps a sync toggle (theme, app-language or study-suggestion preference) while it is in a blocked state and cannot actually be switched on.

## Trigger source

Three trigger points:

- `feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.SyncToggleBlockedClicked`
- `feature/preferences/app_language/src/commonMain/kotlin/com/quare/bibleplanner/feature/applanguage/presentation/AppLanguageViewModel.kt` — `AppLanguageUiEvent.SyncToggleBlockedClicked`
- `feature/preferences/study_suggestion/src/commonMain/kotlin/com/quare/bibleplanner/feature/studysuggestion/presentation/viewmodel/StudySuggestionViewModel.kt` — `StudySuggestionUiEvent.SyncToggleBlockedClicked`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `theme_selection` | Which surface hosted the blocked toggle: `theme_selection` \| `app_language` \| `study_suggestion` |

## Notes

- Complements [setting_sync_toggled](setting_sync_toggled.md), which only fires when the toggle is actually actionable.
- Related: [login_nudge_shown](login_nudge_shown.md).
