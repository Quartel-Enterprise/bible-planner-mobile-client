# device_signed_out

**Tier:** P1 | **Domain:** Auth

Captures the user remotely signing another device out from the connected-devices list (revoking its session). A security-relevant action that also stresses the session-revocation path; its volume flags accounts that feel their session was compromised or shared.

## When it fires

The user taps "Sign out" in the ⋮ menu of a device row that is **not** the current device, in the Account details sheet.

## Trigger source

`feature/account_details/.../presentation/viewmodel/AccountDetailsViewModel.kt` — `AccountDetailsUiEvent.OnSignOutDeviceClick`, non-current-device branch.

## Parameters

None.

## Notes

- Fires on the sign-out action (intent), not on revocation success — the edge-function call can still fail, in which case the row's spinner clears and an error snackbar is shown.
- Signing out the **current** device from the list is not this event: it routes to the normal logout flow, covered by [logout_confirmed](logout_confirmed.md).
- The revoked device ends its own local session as soon as it observes its row disappear (or on its next token refresh); that self-logout is not separately tracked.
