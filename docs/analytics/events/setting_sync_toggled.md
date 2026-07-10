# setting_sync_toggled

**Tier:** P2 | **Domain:** Settings

Captures the user turning cross-device sync on or off for a preference group. Measures adoption of the synced-preferences feature, which is only available to logged-in users and therefore also acts as a soft login incentive.

## When it fires

User flips the "sync" switch in the theme-selection sheet (theme group) or in the app-language sheet (language).

## Trigger source

Two trigger points:

- `feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.SyncToggleClicked(isNewValueOn)`
- `feature/preferences/app_language/src/commonMain/kotlin/com/quare/bibleplanner/feature/applanguage/presentation/AppLanguageViewModel.kt` — `AppLanguageUiEvent.SyncToggleClicked(isNewValueOn)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `setting` | string | `theme` | Which preference group the toggle controls: `theme` \| `language` |
| `is_enabled` | boolean | `true` | New state of the sync switch |

## Notes

- The plan draft listed a `dynamic_colors` value, but the code has no independent sync toggle for dynamic colors: the theme sync switch (`THEME_SYNC_ENABLED`) mirrors theme, contrast and dynamic colors together (`SetThemeSyncEnabledUseCase`). Only `theme` and `language` are valid values.
- When the user is logged out the switch is blocked and emits `SyncToggleBlockedClicked` instead, which routes to the login warning — captured by `login_warning_shown` with `reason` `preferences_theme`/`preferences_language`, not by this event.
- Related: [theme_changed](theme_changed.md), [language_changed](language_changed.md).
