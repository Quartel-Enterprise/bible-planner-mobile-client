# account_delete_cancelled

**Tier:** P2 | **Domain:** Auth

Captures the user backing out of the delete-account dialog. Paired with [account_delete_confirmed](account_delete_confirmed.md) it gives the drop-off rate of the confirmation step, which tells whether the dialog is doing its job as a guard or whether users are opening it by accident.

## When it fires

The user taps "Cancelar" in the delete-account dialog, or dismisses it, while it is still in the idle (not deleting) state.

## Trigger source

`feature/delete_account/.../DeleteAccountUiEvent.kt` — `OnCancel` and `OnDismiss`, both emitted automatically by `TrackedViewModel`.

## Parameters

None.

## Notes

- Only the idle dialog can be dismissed: while the deletion runs, back press and outside taps are disabled, so no event fires mid-deletion.
- The dialog impression is covered by `destination_view` (`destination_name=delete_account`).
- Related: [account_delete_confirmed](account_delete_confirmed.md).
