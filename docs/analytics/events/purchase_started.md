# purchase_started

**Tier:** P1 | **Domain:** Monetization

Captures the moment the user commits to buying Pro and the store purchase flow is launched. Together with [purchase_completed](purchase_completed.md) and [purchase_failed](purchase_failed.md) it measures store-sheet abandonment, the biggest leak at the bottom of the funnel.

## When it fires

The user taps the "start Pro journey" button, the login check passes, a matching store package is resolved, and `GetPurchaseResultUseCase` is invoked (i.e. right before the native purchase sheet appears).

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnStartProJourneyClick` (log at the point `purchaseInitiated = true` is set, after `packageToPurchase` is resolved)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `subscription_plan` | string | `annual` | Plan being purchased: `monthly` \| `annual` |
| `package_id` | string | `$rc_annual` | `StorePackage.identifier` (RevenueCat package id) |
| `price` | string | `R$ 84,90` | `StorePackage.priceString`, localized display price |
| `store` | string | `play_store` | `app_store` \| `play_store` (from `Platform.isApple()`) |

## Notes

- Not fired when the tap is blocked: user not logged in (routed to the login warning instead — that impression is covered by [destination_view](destination_view.md) with `reason=purchase`), a purchase already in flight (`isPurchasing`), or no matching store package.
- Exactly one of [purchase_completed](purchase_completed.md) or [purchase_failed](purchase_failed.md) follows each `purchase_started`.
