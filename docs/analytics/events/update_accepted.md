# update_accepted

**Tier:** P1 | **Domain:** Updates

Captures the user choosing to update from the "Atualização disponível" sheet. Together with [update_prompt_shown](update_prompt_shown.md) it gives the accept rate of the update prompt, split by `source`.

## When it fires

The user taps "Abrir na App Store" on the update sheet, which opens the store. **iOS only** — Android never shows the sheet (it goes straight to the Google Play flexible update flow), so the accept/decline step there happens inside Play's own dialog and is not observable.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateViewModel.kt` — `InAppUpdateUiEvent.OnUpdateClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `manual` | Where the prompt came from: `startup` \| `manual` (propagated from `InAppUpdateNavRoute`) |

## Notes

- Platform is not a parameter (Firebase segments by platform automatically), but in practice every event comes from iOS.
- On Android, a successful download arrives as the [update_downloaded](destination_view.md) impression (`destination_name=update_downloaded`) with no preceding `update_accepted`; a failed one as [update_download_failed](update_download_failed.md).
- The final successful install is covered by Firebase's auto-collected `app_update` event on next launch, so it is not logged manually.
