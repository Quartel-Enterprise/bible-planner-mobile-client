# theme_selection_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the theme-selection sheet without necessarily changing anything. Complements [theme_changed](theme_changed.md) and [contrast_changed](contrast_changed.md) to show how often the sheet is opened and dismissed without a selection.

## When it fires

User dismisses the theme-selection sheet (scrim tap, close action, or system back).

## Trigger source

`feature/preferences/theme_selection/src/commonMain/kotlin/com/quare/bibleplanner/feature/themeselection/presentation/ThemeSelectionViewModel.kt` — `ThemeSelectionUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`theme_selection`, `responsive`).
- Related: [theme_changed](theme_changed.md), [contrast_changed](contrast_changed.md).
