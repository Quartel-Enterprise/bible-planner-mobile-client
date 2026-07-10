# restore_completed

**Tier:** P1 | **Domain:** Monetization

Captures a successful "restore purchases" on the paywall — an existing subscriber recovering their Pro entitlement on a new device or after reinstall. High restore volume relative to purchases indicates device churn rather than new revenue.

## When it fires

`GetRestorePurchaseResultUseCase` returns success after the user taps restore purchases, right before navigating to the Congrats screen.

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnRestorePurchases`, `onSuccess` branch of `getRestorePurchaseResultUseCase()`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `store` | string | `app_store` | `app_store` \| `play_store` |

## Notes

- Requires login: an anonymous tap routes to the login warning (covered by [destination_view](destination_view.md) with `reason=purchase`) and nothing is logged here.
- Not a new conversion — never count it together with [purchase_completed](purchase_completed.md) in revenue funnels.
- Failure counterpart: [restore_failed](restore_failed.md).
