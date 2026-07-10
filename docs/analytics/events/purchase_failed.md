# purchase_failed

**Tier:** P1 | **Domain:** Monetization

Captures a purchase attempt that did not convert, split by reason. Distinguishing user cancellation from technical failures (network, pending payment) separates a pricing/UX problem from an infrastructure problem.

## When it fires

`GetPurchaseResultUseCase` returns failure after a purchase was started (store sheet cancelled, network error, payment pending, or an unexpected billing error).

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnStartProJourneyClick`, `onFailure` branch of `getPurchaseResultUseCase(packageToPurchase)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `user_cancelled` | `user_cancelled` \| `network` \| `payment_pending` \| `unknown`, derived from the `BillingException` subtype the same way `PaywallExceptionMapper` maps it |
| `subscription_plan` | string | `annual` | Plan that was being purchased: `monthly` \| `annual` |
| `store` | string | `play_store` | `app_store` \| `play_store` |

## Notes

- Reason mapping mirrors `feature/paywall/.../mapper/PaywallExceptionMapper.kt`: `BillingException.UserCancelled` → `user_cancelled`, `NetworkError` → `network`, `PaymentPending` → `payment_pending`, everything else → `unknown`.
- `payment_pending` is not a hard failure — the store may still settle it later; watch for a Pro entitlement arriving without a matching [purchase_completed](purchase_completed.md).
- Restore failures are logged as [restore_failed](restore_failed.md), not here.
