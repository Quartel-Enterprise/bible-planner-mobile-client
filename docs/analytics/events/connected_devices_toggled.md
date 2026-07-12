# connected_devices_toggled

**Tier:** P2 | **Domain:** Account

Captures the user expanding or collapsing the "Connected devices" section inside the Account details sheet. Reveals how many users are curious about their active sessions (a security/trust surface) versus ignoring it.

## When it fires

The user taps the "Connected devices" row header in the Account details sheet, toggling the device list open or closed.

## Trigger source

`feature/account_details/.../presentation/viewmodel/AccountDetailsViewModel.kt` — `AccountDetailsUiEvent.OnToggleDevices`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_expanded` | boolean | `true` | `true` when the section is being opened, `false` when collapsed |

## Notes

- The Account details sheet impression itself is covered by [destination_view](destination_view.md) (`destination_name=account_details`).
- Per the toggle convention this is one event with an `is_*` boolean, not separate open/close events.
