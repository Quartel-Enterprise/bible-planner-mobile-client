# material_you_info_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the Material You explainer dialog opened from the Android color-scheme bottom sheet. Complements [material_you_info_clicked](material_you_info_clicked.md) to show how often the explainer is opened and then dismissed.

## When it fires

User dismisses the Material You information dialog (scrim tap, close action, or system back) on the Android color-scheme bottom sheet.

## Trigger source

`feature/material_you/src/commonMain/kotlin/com/quare/bibleplanner/feature/materialyou/presentation/viewmodel/AndroidColorSchemeViewModel.kt` — `AndroidColorSchemeUiEvent.OnInformationDialogDismiss`

## Parameters

None.

## Notes

- Destination impression for the Material You bottom sheet is covered by [destination_view](destination_view.md) (`material_you`, `dialog`).
- Related: [material_you_info_clicked](material_you_info_clicked.md), [material_you_got_it_clicked](material_you_got_it_clicked.md).
