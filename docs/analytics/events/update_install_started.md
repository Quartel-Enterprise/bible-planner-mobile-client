# update_install_started

**Tier:** P2 | **Domain:** Updates

Captures the app installing a downloaded update (Android only). It closes the flexible-update funnel: every successful download ends here, since the app restarts to install it without asking.

## When it fires

Right before Play Core's `completeUpdate()` is called, which restarts the app to finish installing. Two moments lead there:

- The flexible-update download finishes while the app is running.
- The app is opened (or the user taps "Buscar atualizações") and finds an update that was downloaded earlier but not installed yet, e.g. because the download finished after the app was closed.

## Trigger source

`core/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/core/inappupdate/domain/usecase/impl/InstallDownloadedUpdateUseCase.kt`, called from `InAppUpdateDownloadViewModel` (download finished) and `ShowUpdatePromptUseCase` (already downloaded).

## Parameters

_None._

## Notes

- **Android only**: iOS updates go through the App Store, and desktop has no updater.
- **Meaning changed in 2.14.0.** Up to 2.13.0 it fired only when the user tapped "Reiniciar agora" on the "Atualização baixada" sheet; from 2.14.0 the sheet is gone and it fires on every install, with no user choice. Filter by `app_version` when a funnel spans both.
- Versions up to 2.13.0 still send `update_install_postponed` (the user tapped "Mais tarde" or closed the sheet) and a [screen_view](screen_view.md) with `screen_name=update_downloaded` (the sheet opening). 2.14.0 and later never send either, so both are no longer in the catalog.
- It fires once per download in a process: if `completeUpdate()` doesn't go through and the install is retried later in the same session, the retry isn't logged again.
- If the app is in the background when `completeUpdate()` runs, Play installs the update silently without restarting it in front of the user.
- This marks the install *start*; the actual version change is confirmed by the auto-collected `app_update` event on the next launch.
