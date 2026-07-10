# dynamic_colors_toggled

**Tier:** P2 | **Domain:** Settings

Captures the user enabling or disabling Material You dynamic colors (Android-only feature, wallpaper-derived color scheme). Shows how many Android users prefer the dynamic scheme over the app's own palette.

## When it fires

User flips the Material You switch — either in the theme-selection sheet or in the dedicated Material You bottom sheet.

## Trigger source

Two trigger points:

- `feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.MaterialYouToggleClicked(isNewValueOn)`
- `feature/material_you/src/commonMain/kotlin/com/quare/bibleplanner/feature/materialyou/presentation/viewmodel/AndroidColorSchemeViewModel.kt` — `AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange(isEnabled)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | boolean | `true` | New state of the dynamic-colors switch |
| `source` | string | `theme_selection` | Which surface hosted the toggle: `theme_selection` \| `material_you` |

## Notes

- Android-only: the toggle is not rendered on iOS/desktop.
- The dynamic-colors value is mirrored to synced preferences under the theme sync toggle — there is no separate sync switch for it (see [setting_sync_toggled](setting_sync_toggled.md)).
- Related: [theme_changed](theme_changed.md); Material You sheet impressions are covered by [destination_view](destination_view.md) (`material_you`).
