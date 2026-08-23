# day_reading_complete_dismissed

**Tier:** P2 | **Domain:** DayReadingComplete

Captures the user closing the day-reading-complete sheet without tapping the CTA. Complements
[day_reading_complete_cta_clicked](day_reading_complete_cta_clicked.md) to show how often the
celebration moment converts into an AI study versus being dismissed outright.

## When it fires

User dismisses the sheet — close button, scrim tap, or system back.

## Trigger source

`feature/day_reading_complete/.../presentation/viewmodel/DayReadingCompleteViewModel.kt` —
`DayReadingCompleteUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [screen_view](screen_view.md)
  (`day_reading_complete`, `responsive`).
- Related: [day_reading_complete_shown](day_reading_complete_shown.md).
