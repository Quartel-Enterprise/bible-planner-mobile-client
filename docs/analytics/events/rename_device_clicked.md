# rename_device_clicked

**Tier:** P2 | **Domain:** Auth

Captures the user tapping the rename action on a connected device row on the Account Details screen, the entry point into the device-rename flow.

## When it fires

User taps the rename action on a device row on the Account Details screen.

## Trigger source

`feature/account_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/accountdetails/presentation/viewmodel/AccountDetailsViewModel.kt` — `AccountDetailsUiEvent.OnRenameDeviceClick(device: DeviceUiModel)`

## Parameters

None.

## Notes

- Navigates to `RenameDeviceNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`rename_device`, `dialog`).
- The actual rename outcome is a separate, manually-tracked event once wired (see `DEVICE_RENAMED`/`DEVICE_RENAME_CANCELLED` in `AnalyticsEventNames.kt`).
