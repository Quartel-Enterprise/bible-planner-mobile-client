# restore_failed

**Tier:** P1 | **Domain:** Monetization

Captures a failed "restore purchases" attempt. A spike here means legitimate subscribers cannot recover Pro — a direct support-ticket and refund driver.

## When it fires

`GetRestorePurchaseResultUseCase` returns failure after the user taps restore purchases; a snackbar with the mapped error is shown.

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnRestorePurchases`, `onFailure` branch of `getRestorePurchaseResultUseCase()`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `network` | `user_cancelled` \| `network` \| `payment_pending` \| `restore_failed` \| `unknown`, derived from the `BillingException` subtype |
| `store` | string | `app_store` | `app_store` \| `play_store` |

## Notes

- Reason mapping mirrors `feature/paywall/.../mapper/PaywallExceptionMapper.kt`; `BillingException.RestorePurchaseFailed` (typically "nothing to restore") gets its own `restore_failed` value so it is not lost inside `unknown`.
- "Nothing to restore" often means the user never subscribed on that store account — expect a baseline of these from free users poking the button.
- Success counterpart: [restore_completed](restore_completed.md).
