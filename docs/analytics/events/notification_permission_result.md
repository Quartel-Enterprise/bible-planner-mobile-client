# notification_permission_result

**Tier:** P1 | **Domain:** Settings

Captures the outcome of the system notification-permission prompt. Grant rate gates every notification-based feature (currently the Bible-version download-complete notification) and tells us whether the permission flow needs rework.

## When it fires

The OS returns the result of the notification-permission request launched from the in-app permission dialog.

## Trigger source

`feature/notification_permission/src/commonMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/viewmodel/NotificationPermissionViewModel.kt` — `NotificationPermissionUiEvent.OnPermissionResult(granted, canAskAgain)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_granted` | boolean | `false` | Whether the user granted the permission |
| `can_ask_again` | boolean | `false` | Whether the OS still allows re-prompting; `false` with `is_granted=false` means permanently denied (dialog switches to its "open settings" state) |

## Notes

- Android 13+ only: the startup prompt in `NotificationPermissionStartEffect.android.kt` reports its result directly to the launcher callback, not through this UiEvent — instrumentation should cover both paths or log this event only for the dialog-driven flow and note the gap.
- iOS requests authorization at launch in `iosApp/iosApp/iOSApp.swift` (`requestAuthorization`) outside this ViewModel; wiring that result into this event is an implementation decision.
- Related: [notification_permission_prompted](notification_permission_prompted.md), [notification_opened](notification_opened.md).
