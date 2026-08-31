# day_reading_complete_banner_cta_clicked

**Tier:** P1 | **Domain:** DayReadingComplete

Captures the user acting on the banner's call to action — generating or viewing the day study, or heading to the paywall when the free quota is gone.

## When it fires

The user taps the banner's CTA line while its state is resolved. Taps during the quota shimmer do nothing and log nothing.

## Trigger source

`feature/day_reading_complete/.../presentation/model/DayReadingCompleteBannerUiEvent.kt` — `DayReadingCompleteBannerUiEvent.OnCtaClick`, tracked from `DayReadingCompleteBannerViewModel.onCtaClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `account_state` | string | `free` | `free`, `free_exhausted` or `pro` — what the CTA offered when tapped |
| `source` | string | `day_reading_complete_banner` | Constant, distinguishing the banner from the sheet's CTA |

## Notes

- The sheet's equivalent is [day_reading_complete_cta_clicked](day_reading_complete_cta_clicked.md); together with the two shown events they compare conversion of the two presentations.
- With `account_state = free_exhausted` the tap opens the paywall instead of generating.
