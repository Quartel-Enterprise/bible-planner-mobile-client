# contrast_changed

**Tier:** P2 | **Domain:** Settings

Captures the user changing the color-contrast level in the theme-selection sheet. Contrast usage indicates demand for the accessibility variants of the color scheme.

## When it fires

User selects a contrast option (standard, medium or high) in the theme-selection sheet.

## Trigger source

`feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.OnContrastSelected(contrastType: ContrastType)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `contrast` | string | `high` | Selected `ContrastType` value: `standard` \| `medium` \| `high` |

## Notes

- Related: [theme_changed](theme_changed.md). Contrast is mirrored to the synced preferences together with the theme when theme sync is on (see [setting_sync_toggled](setting_sync_toggled.md)).
