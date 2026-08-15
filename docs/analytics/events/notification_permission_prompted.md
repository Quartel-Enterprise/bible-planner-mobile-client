# notification_permission_prompted

**Tier:** P2 | **Domain:** Settings

Captures every time the user is asked for notification permission — the just-in-time system prompt when a Bible-version download starts or the in-app rationale dialog after a denial. Denominator for the grant rate measured by [notification_permission_result](notification_permission_result.md).

## When it fires

- The system permission prompt is launched when a user-initiated Bible-version download starts (download, resume, or update on the Bible-versions screen, the pending-updates dialog, or the reader's "download selected version" error CTA) and the prompt can still be shown (Android 13+ with the permission not granted; iOS with authorization not yet determined).
- The in-app permission dialog re-requests the system prompt (`OnConfirm` while `isFirstTime` is true).

## Trigger source

Two trigger points:

- `core/provider/platform/src/commonMain/kotlin/com/quare/bibleplanner/core/provider/platform/domain/usecase/RequestDownloadNotificationPermissionUseCase.kt` — invoked by `BibleVersionViewModel` (download/resume/update), `PendingBibleUpdatesViewModel` (update), and `ReadViewModel` (download CTA)
- `feature/notification_permission/src/commonMain/kotlin/com/quare/bibleplanner/feature/notificationpermission/presentation/viewmodel/NotificationPermissionViewModel.kt` — `NotificationPermissionUiEvent.OnConfirm` → `NotificationPermissionUiAction.RequestSystemPermission`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_first_time` | boolean | `true` | `true` for the download-triggered system prompt; from `NotificationPermissionUiState.isFirstTime` for the dialog flow (`false` means the dialog is in its "open settings" state and no system prompt can be shown) |

## Notes

- Fires on Android 13+ and iOS; the JVM requester is inert. On iOS the prompt only fires while authorization is not determined, so it happens at most once per install.
- On Android the prompt can fire more than once per session: every user-initiated download start while the permission is denied-but-askable re-launches the system prompt.
- Auto-resume of an in-progress download at startup and the iOS Live Activity resume deep link never prompt.
- The permission dialog itself is a NavKey (`notification_permission`), so its impressions are also covered by [screen_view](screen_view.md).
- Related: [notification_permission_result](notification_permission_result.md).
