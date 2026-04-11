package com.quare.bibleplanner.notification

import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.notification_complete_message
import bibleplanner.composeapp.generated.resources.notification_complete_title
import bibleplanner.composeapp.generated.resources.notification_error_message
import bibleplanner.composeapp.generated.resources.notification_error_title
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.NavigationEventBus
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBibleVersionDownloadNotifier(
    navigationEventBus: NavigationEventBus,
) : BibleVersionDownloadNotifier {
    init {
        NotificationTapRouter.setNavigationEventBus(navigationEventBus)
    }

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) = Unit

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) = Unit

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(getString(Res.string.notification_complete_title, versionName))
            setBody(getString(Res.string.notification_complete_message))
        }
        val request = UNNotificationRequest.requestWithIdentifier(completeId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(getString(Res.string.notification_error_title, versionName))
            setBody(getString(Res.string.notification_error_message))
        }
        val request = UNNotificationRequest.requestWithIdentifier(errorId(versionId), content, trigger = null)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun dismiss(versionId: String) {
        removeNotifications(listOf(progressId(versionId), completeId(versionId), errorId(versionId)))
    }

    private fun progressId(versionId: String): String = "${versionId}_progress"

    private fun completeId(versionId: String): String = "${versionId}_complete"

    private fun errorId(versionId: String): String = "${versionId}_error"

    private fun removeNotifications(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

}
