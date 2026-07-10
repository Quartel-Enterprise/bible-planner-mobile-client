# logout_failed

**Tier:** P1 | **Domain:** Auth

Captures a confirmed logout that could not complete. The `pending_changes` reason measures how often the pre-logout flush of unsynced data fails (usually offline), which is the main source of the forced-logout escape hatch; `unknown` covers sign-out or local-data-clear errors.

## When it fires

`Logout` returns failure after the user confirmed the logout dialog.

## Trigger source

`feature/logout/.../LogoutViewModel.kt` — failure branch of `LogoutUiEvent.ConfirmLogoutClick`:

- `LogoutFlushFailedException` → `reason=pending_changes` (state becomes `LogoutUiState.PendingChangesError`, offering force logout)
- any other throwable → `reason=unknown` (error snackbar via `LogoutErrorMapper`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `pending_changes` | `pending_changes` (flush of unsynced changes failed; logout aborted) \| `unknown` (sign-out or local clear failed) |

## Notes

- After `reason=pending_changes` the dialog offers "sign out anyway"; if taken, a [logout_confirmed](logout_confirmed.md) with `is_forced=true` follows.
- The flush is aborted deliberately so unsynced changes are not silently lost — the user stays logged in and can retry (see `LogoutUseCase` KDoc for the teardown order).
- Related: [logout_confirmed](logout_confirmed.md).
