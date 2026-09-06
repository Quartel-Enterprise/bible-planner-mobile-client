# google_account_add_declined

**Tier:** P2 | **Domain:** Auth

Captures the user declining the prompt to add a Google account on-device, the negative counterpart to [google_account_add_confirmed](google_account_add_confirmed.md). A high decline rate signals friction at the point where Google sign-in could not get a credential from the device.

## When it fires

The user dismisses the Google-unavailable dialog shown during login instead of tapping "add account".

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.DismissAddGoogleAccountDialog`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Pairs with [google_account_add_confirmed](google_account_add_confirmed.md) to give the full accept/decline split for this dialog.
- Declining only closes the dialog; no external system flow is launched.
