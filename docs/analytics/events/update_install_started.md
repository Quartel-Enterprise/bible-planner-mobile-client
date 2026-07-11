# update_install_started

**Tier:** P2 | **Domain:** Updates

Captures the user choosing to restart and install a downloaded update immediately (Android only). It measures how many users complete the flexible-update flow right away versus postponing with "Mais tarde".

## When it fires

On the "Atualização baixada" dialog, the user taps "Reiniciar agora", which calls Play Core's `completeUpdate()` and restarts the app to finish installing.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/UpdateDownloadedViewModel.kt` — `UpdateDownloadedUiEvent.OnRestartNowClick`.

## Parameters

_None._

## Notes

- **Android only** — this dialog exists only for the flexible in-app update flow.
- Tapping "Mais tarde" is deliberately not logged (it is a postpone, not an action); the install then completes silently on a later app restart, covered by Firebase's auto-collected `app_update`.
- This marks install *intent*; the actual version change is confirmed by the auto-collected `app_update` event on the next launch.
