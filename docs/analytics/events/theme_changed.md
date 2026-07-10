# theme_changed

**Tier:** P1 | **Domain:** Settings

Captures the user picking a theme in the theme-selection bottom sheet. Theme preference correlates with retention and informs how much to invest in dark-theme polish; it also backs the proposed `app_theme` user property.

## When it fires

User selects a theme option (light, dark or system) in the theme-selection sheet.

## Trigger source

`feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.OnThemeSelected(theme: Theme)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `theme` | string | `dark` | Selected `Theme` value: `light` \| `dark` \| `system` |

## Notes

- Fires on every selection, including re-selecting the current theme — instrumentation may skip no-op selections.
- Related: [contrast_changed](contrast_changed.md), [dynamic_colors_toggled](dynamic_colors_toggled.md), [setting_sync_toggled](setting_sync_toggled.md). Sheet impressions are covered by [destination_view](destination_view.md) (`theme_selection`).
- Should also refresh the proposed `app_theme` user property (see README).
