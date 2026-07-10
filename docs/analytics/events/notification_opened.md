# notification_opened

**Tier:** P2 | **Domain:** Settings

Captures the user tapping a local notification and being routed back into the app. Measures whether notifications actually re-engage users, justifying (or not) adding more notification types.

## When it fires

User taps a notification and the app routes the tap to a destination — today always the Bible-version manager after a version download completes.

## Trigger source

Platform tap-routing glue, not a ViewModel:

- Android: `shared/src/androidMain/kotlin/com/quare/bibleplanner/MainActivity.kt` — `handleNotificationIntent` (checks the `EXTRA_NAVIGATE_TO_BIBLE_VERSIONS` extra, sends `BibleVersionSelectorRoute` on the `NavigationEventBus`)
- iOS: `iosApp/iosApp/NotificationDelegate.swift` — `didReceive` → `NotificationTapRouter.routeToBibleVersions()` (`shared/src/iosMain/kotlin/com/quare/bibleplanner/notification/NotificationTapRouter.kt`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `type` | string | `version_download_complete` | Notification type. Only `version_download_complete` exists today |

## Notes

- The code carries no explicit type field (Android uses a boolean intent extra; iOS routes every tap unconditionally) — `type` is a documentation convention so the event scales when more notification types are added.
- Handles both cold start (`onCreate`) and warm resume (`onNewIntent`) on Android.
- Related: [notification_permission_result](notification_permission_result.md), [bible_version_download_completed](bible_version_download_completed.md).
