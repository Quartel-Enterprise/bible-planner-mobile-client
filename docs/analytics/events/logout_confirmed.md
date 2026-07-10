# logout_confirmed

**Tier:** P1 | **Domain:** Auth

Captures the user confirming sign-out in the logout dialog. Logout is a churn-adjacent signal for a sync-centric app, and the `is_forced` split reveals how often users sign out *despite* unsynced changes (accepting data loss), which points at flush reliability problems.

## When it fires

The user taps the confirm button in the logout dialog — either the normal confirmation or the "sign out anyway" button offered after a failed flush of pending changes.

## Trigger source

`feature/logout/.../LogoutViewModel.kt` — two trigger points:

- `LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout` — normal confirm (`is_forced=false`; pending changes are flushed first)
- `LogoutUiEvent.ConfirmLogoutClick.OnForceLogout` — sign out anyway after a `PendingChangesError` (`is_forced=true`; the flush is skipped and unsynced changes are dropped)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_forced` | boolean | `false` | `true` when the user chose to sign out without flushing pending changes after a flush failure (`shouldFlushPending=false`) |

## Notes

- Fires on the confirmation click, not on logout success — a confirmed logout can still end in [logout_failed](logout_failed.md).
- Cancelling or dismissing the dialog is deliberately untracked (per the "not tracked" list); the dialog impression is covered by `destination_view` (`destination_name=logout`).
- An `is_forced=true` event is always preceded by a [logout_failed](logout_failed.md) with `reason=pending_changes` in the same session.
