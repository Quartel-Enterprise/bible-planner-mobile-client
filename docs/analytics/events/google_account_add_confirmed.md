# google_account_add_confirmed

**Tier:** P2 | **Domain:** Auth

Captures the user confirming the prompt to add a Google account on-device, launched when the login flow needs a Google account that isn't yet present.

## When it fires

The user taps confirm on the "add Google account" dialog shown during login; the app launches the system add-account flow.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.AddGoogleAccountConfirmClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Captures the decision to add an account, not the outcome — the system flow's result isn't observable by the app.
- Dismissing the dialog instead (`DismissAddGoogleAccountDialog`) is not tracked; it only closes the dialog with no external effect.
