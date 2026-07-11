# update_accepted

**Tier:** P1 | **Domain:** Updates

Captures the user choosing to update from the "Atualização disponível" sheet. Together with [update_prompt_shown](update_prompt_shown.md) it gives the accept rate of the update prompt, split by `source`.

## When it fires

The user taps the primary call-to-action on the update sheet — "Atualizar" on Android (starts the Google Play flexible update flow) or "Abrir na App Store" on iOS (opens the store).

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateViewModel.kt` — `InAppUpdateUiEvent.OnUpdateClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `manual` | Where the prompt came from: `startup` \| `manual` (propagated from `InAppUpdateNavRoute`) |

## Notes

- Platform is not a parameter (Firebase segments by platform automatically): on Android this precedes the in-app download; on iOS it hands off to the App Store.
- On Android, a successful download that follows arrives as the [update_downloaded](destination_view.md) impression (`destination_name=update_downloaded`); a failed one as [update_download_failed](update_download_failed.md).
- The final successful install is covered by Firebase's auto-collected `app_update` event on next launch, so it is not logged manually.
