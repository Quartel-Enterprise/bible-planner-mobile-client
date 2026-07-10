# purchase_completed

**Tier:** P1 | **Domain:** Monetization

Captures a successful Pro subscription purchase. This is the primary monetization conversion; combined with `paywall_viewed.source` it attributes revenue to the feature gate that triggered the purchase.

## When it fires

`GetPurchaseResultUseCase` returns success after the store purchase sheet completes, right before navigating to the Congrats screen.

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnStartProJourneyClick`, `onSuccess` branch of `getPurchaseResultUseCase(packageToPurchase)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `subscription_plan` | string | `annual` | Purchased plan: `monthly` \| `annual` |
| `package_id` | string | `$rc_annual` | `StorePackage.identifier` (RevenueCat package id) |
| `price` | string | `R$ 84,90` | `StorePackage.priceString`, localized display price |
| `store` | string | `play_store` | `app_store` \| `play_store` |

## Notes

- Firebase already auto-logs the standard `in_app_purchase` event (with revenue) on Android and iOS; do **not** duplicate revenue reporting. This custom event exists to carry funnel context (`subscription_plan`, `package_id`, `store`) and to exist on Desktop, where nothing is auto-collected.
- Revenue truth lives in RevenueCat; treat this event as a funnel marker, not an accounting source.
- Follows [purchase_started](purchase_started.md); restores use [restore_completed](restore_completed.md) instead.
- The Congrats screen impression that follows is covered by [destination_view](destination_view.md).
