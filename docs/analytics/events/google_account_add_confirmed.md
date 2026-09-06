# google_account_add_confirmed

**Tier:** P2 | **Domain:** Auth

Captures the user confirming the prompt to add a Google account on-device, launched when Google sign-in could not get a credential from the device.

## When it fires

The user taps "add account" on the Google-unavailable dialog shown during login; the app launches the system add-account flow.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.AddGoogleAccountConfirmClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Captures the decision to add an account, not the outcome — the system flow's result isn't observable by the app.
- Dismissing the dialog instead is tracked as [google_account_add_declined](google_account_add_declined.md).
- The dialog also covers devices that *do* have a Google account but whose credential request was rejected, so confirmations here are not proof that an account was missing.
