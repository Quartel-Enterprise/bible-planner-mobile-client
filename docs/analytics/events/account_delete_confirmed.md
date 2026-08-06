# account_delete_confirmed

**Tier:** P1 | **Domain:** Auth

Captures the user confirming the permanent deletion of their account. This is the strongest churn signal the app has — the account and all its synced data are destroyed server-side — and the `is_pro` split shows how often a paying user leaves without cancelling the store subscription first.

## When it fires

The user taps "Excluir conta" in the delete-account dialog after typing the confirmation keyword. It fires when the deletion starts, not when it succeeds.

## Trigger source

`feature/delete_account/.../DeleteAccountViewModel.kt` — the `DeleteAccountUiEvent.OnConfirmDelete` branch, after the typed-keyword guard passes.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_pro` | boolean | `false` | `true` when the user had an active Pro entitlement at confirmation time — the case where the dialog also warned that the store subscription is not cancelled automatically |

## Notes

- Fires on the confirmation click, not on success — a confirmed deletion can still end in [account_delete_failed](account_delete_failed.md).
- The dialog impression is covered by `destination_view` (`destination_name=delete_account`); reaching it is covered by [profile_option_clicked](profile_option_clicked.md) with `option=delete_account`.
- Deleting the account does not cancel a Google Play / App Store subscription; an `is_pro=true` event is a candidate for a support follow-up.
- Related: [account_delete_cancelled](account_delete_cancelled.md), [logout_confirmed](logout_confirmed.md).
