# notification_permission_prompted

**Tier:** P2 | **Domain:** Settings

Captures every time the user is asked for notification permission — the silent system prompt at startup or the in-app rationale dialog after a denial. Denominator for the grant rate measured by [notification_permission_result](notification_permission_result.md).

## When it fires

- The system permission prompt is launched at app start (Android 13+, permission not granted, no rationale required).
- The in-app permission dialog re-requests the system prompt (`OnConfirm` while `isFirstTime` is true).

## Trigger source

Two trigger points:

- `feature/notification_permission/src/androidMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/NotificationPermissionStartEffect.android.kt` — `LaunchedEffect` launching `POST_NOTIFICATIONS`
- `feature/notification_permission/src/commonMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/viewmodel/NotificationPermissionViewModel.kt` — `NotificationPermissionUiEvent.OnConfirm` → `NotificationPermissionUiAction.RequestSystemPermission`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_first_time` | boolean | `true` | `true` for the startup system prompt; from `NotificationPermissionUiState.isFirstTime` for the dialog flow (`false` means the dialog is in its "open settings" state and no system prompt can be shown) |

## Notes

- Android 13+ only today: the iOS and JVM `NotificationPermissionStartEffect` actuals are no-ops. iOS asks for authorization at launch in `iosApp/iosApp/iOSApp.swift`, outside this flow.
- The permission dialog itself is a NavKey (`notification_permission`), so its impressions are also covered by [destination_view](destination_view.md).
- Related: [notification_permission_result](notification_permission_result.md).
