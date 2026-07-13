# congrats_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures the user closing the congrats celebration bottom sheet without tapping the explore action. Complements [congrats_explore_clicked](congrats_explore_clicked.md) to show how often the celebration moment converts into further engagement versus being dismissed outright.

## When it fires

User dismisses the congrats bottom sheet (primary button, scrim tap, or system back).

## Trigger source

`feature/congrats/src/commonMain/kotlin/com/quare/bibleplanner/feature/congrats/presentation/viewmodel/CongratsViewModel.kt` — `CongratsUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`congrats`, `bottom_sheet`).
- Related: [congrats_explore_clicked](congrats_explore_clicked.md).
