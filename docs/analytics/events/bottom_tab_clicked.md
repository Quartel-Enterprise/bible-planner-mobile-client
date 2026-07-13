# bottom_tab_clicked

**Tier:** P2 | **Domain:** Settings

Captures taps on the bottom navigation bar (Plans / Books / More). Click-through on the tab bar is a distinct funnel signal from the resulting screen impression, so it is tracked separately from `destination_view`.

## When it fires

User taps a tab in the bottom navigation bar.

## Trigger source

`feature/main/src/commonMain/kotlin/com/quare/bibleplanner/feature/main/presentation/viewmodel/MainScreenViewModel.kt` — `MainScreenUiEvent.BottomNavItemClicked`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `tab` | string | `plans` | Destination tab: `plans`, `books`, `more` (derived from the `MainNavRouteDestination` the tap navigates to; `unknown` if an unrecognized route type is passed) |

## Notes

- The subsequent screen impression is separately captured by [destination_view](destination_view.md); this event only covers the tap itself, including taps on the already-selected tab.
