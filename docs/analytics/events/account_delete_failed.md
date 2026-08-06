# account_delete_failed

**Tier:** P1 | **Domain:** Auth

Captures a confirmed account deletion that could not complete. The reason split separates a server-side failure (the account still exists — the user can retry) from a local teardown failure (the account is already gone but this device still holds its data), which are very different support situations.

## When it fires

The delete-account flow returns failure after the user confirmed the dialog.

## Trigger source

`feature/delete_account/.../DeleteAccountViewModel.kt` — failure branch of `DeleteAccountUiEvent.OnConfirmDelete`, reading the phase the flow failed in:

- `DeleteAccountPhase.DELETING_DATA` → `reason=delete_request` (the `delete-account` edge function call failed; nothing was deleted)
- `DeleteAccountPhase.CLOSING_ACCOUNT` → `reason=close_session` (the account was deleted, but signing out and clearing local data failed)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `delete_request` | `delete_request` (server-side deletion failed; the account is intact) \| `close_session` (account deleted, local sign-out or data clear failed) |

## Notes

- Both reasons return the dialog to its idle state and show an error snackbar, so a retry is possible; a retry after `close_session` hits an already-deleted account.
- Related: [account_delete_confirmed](account_delete_confirmed.md), [logout_failed](logout_failed.md).
