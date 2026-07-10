# update_dismissed

**Tier:** P2 | **Domain:** Updates

Captures the user declining the "Atualização disponível" sheet. It is the drop-off counterpart to [update_accepted](update_accepted.md) and, split by `source`, shows whether the startup auto-prompt is perceived as intrusive.

## When it fires

The user dismisses the update sheet without updating — tapping "Agora não" or the close control.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateViewModel.kt` — `InAppUpdateUiEvent.OnDismiss`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `startup` | Where the prompt came from: `startup` \| `manual` (propagated from `InAppUpdateNavRoute`) |

## Notes

- Both the "Agora não" button and the sheet's close/scrim dismissal route through the same `OnDismiss` event, so they are not distinguished.
- Dismissing does not disable future prompts permanently — the startup prompt is only throttled for the current session, so the same user can produce a `startup` `update_dismissed` again on a later launch while the update is still pending.
