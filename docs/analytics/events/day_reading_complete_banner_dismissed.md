# day_reading_complete_banner_dismissed

**Tier:** P2 | **Domain:** DayReadingComplete

Captures the user waving the banner away without acting on it.

## When it fires

The user taps the banner's close button.

## Trigger source

`feature/day_reading_complete/.../presentation/model/DayReadingCompleteBannerUiEvent.kt` — `DayReadingCompleteBannerUiEvent.OnDismissClick`, emitted automatically.

## Parameters

None beyond the standard context.

## Notes

- Pairs with [day_reading_complete_banner_cta_clicked](day_reading_complete_banner_cta_clicked.md) as the two explicit outcomes of a shown banner; leaving the reader with the banner still up produces neither.
- The banner is not a nav destination, so there is no `screen_view` to correlate — [day_reading_complete_banner_shown](day_reading_complete_banner_shown.md) is the impression signal.
