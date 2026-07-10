# update_download_failed

**Tier:** P2 | **Domain:** Updates

Captures the Google Play flexible in-app update download aborting with an error (Android only). The failure rate against [update_accepted](update_accepted.md) surfaces how reliable the in-app update path is in the field.

## When it fires

The Play Core install-state listener reports `InstallStatus.FAILED` for an in-progress flexible update. A global snackbar informing the user is shown at the same time.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateDownloadViewModel.kt` — `UpdateDownloadState.Failed` branch (fed by `AndroidInAppUpdater`'s `InstallStateUpdatedListener`).

## Parameters

_None._

## Notes

- **Android only.** iOS hands off to the App Store and never downloads in-app; Desktop is a no-op. The download-state flow is empty on those platforms, so this event cannot fire there.
- A successful download instead produces the [update_downloaded](destination_view.md) dialog impression (`destination_name=update_downloaded`); there is no separate `update_download_completed` event because that impression already marks the milestone.
- Play Core does not expose a typed failure reason here, so no `reason` parameter is logged.
