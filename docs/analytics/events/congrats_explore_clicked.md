# congrats_explore_clicked

**Tier:** P2 | **Domain:** Monetization

Captures the user tapping the explore action on the congrats celebration bottom sheet, choosing to keep browsing instead of just closing the sheet. Complements [congrats_dismissed](congrats_dismissed.md) to show how often the celebration moment converts into further engagement.

## When it fires

User taps the explore action on the congrats bottom sheet.

## Trigger source

`feature/congrats/src/commonMain/kotlin/com/quare/bibleplanner/feature/congrats/presentation/viewmodel/CongratsViewModel.kt` — `CongratsUiEvent.OnStartExploring`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`congrats`, `bottom_sheet`).
- Related: [congrats_dismissed](congrats_dismissed.md).
