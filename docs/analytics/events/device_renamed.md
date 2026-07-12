# device_renamed

**Tier:** P2 | **Domain:** Account

Captures the user renaming one of their connected devices. A low-frequency personalization signal that indicates engagement with the device-management surface.

## When it fires

The user confirms the rename dialog with a non-empty name that differs from the current one.

## Trigger source

`feature/account_details/.../presentation/viewmodel/RenameDeviceViewModel.kt` — `onConfirm`, when the trimmed name is non-empty and changed.

## Parameters

None. The device name is user content and is intentionally not sent.

## Notes

- Confirming with an unchanged or blank name only closes the dialog and does not fire this event.
- The rename dialog impression is covered by [destination_view](destination_view.md) (`destination_name=rename_device`).
- The rename is offline-first (written locally and synced later), so this event fires even without connectivity.
