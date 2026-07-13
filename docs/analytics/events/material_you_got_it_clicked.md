# material_you_got_it_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping "Got it" to dismiss the Android color-scheme bottom sheet after reading about Material You. Shows how many users acknowledge the explainer versus dismissing it via scrim/back.

## When it fires

User taps the "Got it" button on the Android color-scheme (Material You) bottom sheet.

## Trigger source

`feature/material_you/src/commonMain/kotlin/com/quare/bibleplanner/feature/materialyou/presentation/viewmodel/AndroidColorSchemeViewModel.kt` — `AndroidColorSchemeUiEvent.BottomSheetGotItClick`

## Parameters

None.

## Notes

- Destination impression for the bottom sheet is covered by [destination_view](destination_view.md) (`material_you`, `dialog`).
- Related: [material_you_info_dismissed](material_you_info_dismissed.md).
