# device_rename_cancelled

**Tier:** P3 | **Domain:** Account

Captures the user backing out of the rename-device dialog without saving. Paired with [device_renamed](device_renamed.md), it measures how often the dialog is opened but abandoned.

## When it fires

The user dismisses the rename dialog without a saved change: tapping "Cancel", the scrim, or the system back gesture. All three route through `AlertDialog.onDismissRequest` / the cancel button, both wired to the same event.

## Trigger source

`feature/account_details/.../presentation/viewmodel/RenameDeviceViewModel.kt` — `RenameDeviceUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Confirming with an unchanged or blank name is not tracked as a cancellation — it silently closes the dialog, the same as before the confirm flow existed.
- Pair: [device_renamed](device_renamed.md).
