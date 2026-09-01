# notification_permission_result

**Tier:** P1 | **Domain:** Settings

Captures the outcome of the system notification-permission prompt. Grant rate gates every notification-based feature (currently the Bible-version download notifications) and tells us whether the permission flow needs rework.

## When it fires

The OS returns the result of a notification-permission request — either the just-in-time prompt at a user-initiated download start or the request launched from the in-app permission dialog.

## Trigger source

Two trigger points:

- `core/provider/platform/src/commonMain/kotlin/com/quare/bibleplanner/core/provider/platform/domain/usecase/RequestDownloadNotificationPermissionUseCase.kt` — tracked right after the platform requester returns; a permanently-denied result also navigates to the `notification_permission` rationale dialog
- `feature/notification_permission/src/commonMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/viewmodel/NotificationPermissionViewModel.kt` — `NotificationPermissionUiEvent.OnPermissionResult(granted, canAskAgain)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_granted` | boolean | `false` | Whether the user granted the permission |
| `can_ask_again` | boolean | `false` | Whether the OS still allows re-prompting; `false` with `is_granted=false` means permanently denied (the download flow opens the rationale dialog in its "open settings" state) |

## Notes

- Fires on Android 13+ and iOS. On iOS `can_ask_again` is always `true`: already-denied users are never re-prompted (the requester only prompts while authorization is not determined), so a permanent denial is never reported there.
- Related: [notification_permission_prompted](notification_permission_prompted.md), [notification_opened](notification_opened.md).
