# rate_app_review_requested

**Tier:** P1 | **Domain:** Settings

Captures the outcome of a "Rate the app" tap: whether the native in-app review flow actually launched, or the user was redirected to the store listing instead. Measures how often users get the frictionless native flow versus the store fallback.

## When it fires

Right after the user taps "Rate the app" on the More screen and the app finishes attempting to launch the platform's in-app review flow.

## Trigger source

`feature/more/src/commonMain/kotlin/com/quare/bibleplanner/feature/more/presentation/viewmodel/MoreViewModel.kt` — `rateAppClick()`, called from `MoreOptionItemType.RATE_APP`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `in_app_review` | How the rating prompt was surfaced: `in_app_review` (native Google Play in-app review flow launched successfully) \| `store_redirect` (fell back to opening the store listing) |

## Notes

- On Android, `store_redirect` fires when the Play in-app review API fails to launch (no Play Services, no activity available, quota/API error) — see `AndroidRequestInAppReview` in `core/provider/platform`.
- On iOS and Desktop there is no in-app review integration yet, so every tap logs `store_redirect`; this event is what would show the funnel shift once/if `RequestInAppReview` is implemented for those platforms.
- Reaching the store this way does **not** mean the user actually left a review — the OS review APIs intentionally never report whether the dialog was shown or acted upon, so `in_app_review` only confirms the flow was launched, not completed.
- The click itself (before knowing the outcome) is already captured by [more_option_clicked](more_option_clicked.md) with `option=rate_app`; this event adds the launch-outcome context that one doesn't have.
