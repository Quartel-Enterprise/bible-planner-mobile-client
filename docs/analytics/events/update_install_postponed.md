# update_install_postponed

**Tier:** P2 | **Domain:** Updates

Captures the user postponing an already-downloaded update instead of restarting immediately (Android only). Together with [update_install_started](update_install_started.md) it measures how many users complete the flexible-update flow right away versus deferring it.

## When it fires

On the "Atualização baixada" dialog, the user taps "Mais tarde" instead of restarting.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/model/UpdateDownloadedUiEvent.kt` — `UpdateDownloadedUiEvent.OnLaterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- **Android only** — this dialog exists only for the flexible in-app update flow.
- The install still completes silently on a later app restart, covered by Firebase's auto-collected `app_update`.
- Pair: [update_install_started](update_install_started.md).
