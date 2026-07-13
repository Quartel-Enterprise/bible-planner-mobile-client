# material_you_info_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the info affordance next to the Material You / dynamic-colors toggle in the theme-selection sheet, opening the explainer bottom sheet. Shows how often users seek clarification before enabling the feature.

## When it fires

User taps the Material You info icon on the theme-selection sheet.

## Trigger source

`feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.MaterialYouInfoClicked`

## Parameters

None.

## Notes

- Opens the Material You bottom sheet; its impression is covered by [destination_view](destination_view.md) (`material_you`, `dialog`).
- Related: [material_you_info_dismissed](material_you_info_dismissed.md), [dynamic_colors_toggled](dynamic_colors_toggled.md).
