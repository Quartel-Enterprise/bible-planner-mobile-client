package com.quare.bibleplanner.notification

import bibleplanner.shared.generated.resources.Res
import bibleplanner.shared.generated.resources.notification_complete_message
import bibleplanner.shared.generated.resources.notification_complete_title
import bibleplanner.shared.generated.resources.notification_error_message
import bibleplanner.shared.generated.resources.notification_error_title
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.Navigator
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBibleVersionDownloadNotifier(
    private val liveActivity: IosDownloadLiveActivity,
    navigator: Navigator,
) : BibleVersionDownloadNotifier {
    init {
        NotificationTapRouter.setNavigator(navigator)
    }

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        liveActivity.showProgress(
            versionId = versionId,
            versionName = versionName,
            progress = progress,
        )
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        liveActivity.showPaused(
            versionId = versionId,
            onShown = {},
        )
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(getString(Res.string.notification_complete_title, versionName))
            setBody(getString(Res.string.notification_complete_message))
        }
        val request = UNNotificationRequest.requestWithIdentifier(getCompleteId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
        liveActivity.showComplete(versionId)
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(getString(Res.string.notification_error_title, versionName))
            setBody(getString(Res.string.notification_error_message))
        }
        val request = UNNotificationRequest.requestWithIdentifier(getErrorId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
        liveActivity.dismiss(versionId)
    }

    /*
     * Why: pausing also dismisses, but a paused download keeps its Live Activity with the resume
     * button, so the facade decides when the Live Activity goes away.
     */
    override suspend fun dismiss(versionId: String) {
        removeNotifications(listOf(getProgressId(versionId), getCompleteId(versionId), getErrorId(versionId)))
    }

    private fun getProgressId(versionId: String): String = "${versionId}_progress"

    private fun getCompleteId(versionId: String): String = "${versionId}_complete"

    private fun getErrorId(versionId: String): String = "${versionId}_error"

    private fun removeNotifications(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }
}
