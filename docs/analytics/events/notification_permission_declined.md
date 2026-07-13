# notification_permission_declined

**Tier:** P2 | **Domain:** Settings

Captures the user declining the in-app notification permission rationale dialog. Together with [notification_permission_result](notification_permission_result.md) it distinguishes users who never see the system prompt (declined the rationale) from those who see it and deny.

## When it fires

User taps the decline button in the in-app notification permission dialog, choosing not to proceed to the system permission prompt.

## Trigger source

`feature/notification_permission/src/commonMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/viewmodel/NotificationPermissionViewModel.kt` — `NotificationPermissionUiEvent.OnDecline`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=notification_permission`).
- Related: [notification_permission_prompted](notification_permission_prompted.md), [notification_permission_result](notification_permission_result.md).
