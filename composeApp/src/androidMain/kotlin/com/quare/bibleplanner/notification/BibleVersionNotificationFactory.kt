package com.quare.bibleplanner.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.notification_complete_message
import bibleplanner.composeapp.generated.resources.notification_complete_title
import bibleplanner.composeapp.generated.resources.notification_error_message
import bibleplanner.composeapp.generated.resources.notification_error_title
import bibleplanner.composeapp.generated.resources.notification_paused_message
import bibleplanner.composeapp.generated.resources.notification_paused_title
import bibleplanner.composeapp.generated.resources.notification_preparing_progress
import bibleplanner.composeapp.generated.resources.notification_preparing_starting
import bibleplanner.composeapp.generated.resources.notification_preparing_title
import com.quare.bibleplanner.MainActivity
import org.jetbrains.compose.resources.getString

internal class BibleVersionNotificationFactory(
    private val context: Context,
) {
    suspend fun createProgress(
        versionName: String,
        progress: Float,
    ): Notification {
        val percentInt = (progress * 100).toInt()
        return downloadingBuilder(versionName)
            .setContentText(
                if (percentInt > 0) {
                    getString(Res.string.notification_preparing_progress, percentInt)
                } else {
                    getString(Res.string.notification_preparing_starting)
                },
            ).setProgress(100, percentInt, percentInt == 0)
            .build()
    }

    suspend fun createForeground(versionName: String): Notification = downloadingBuilder(versionName)
        .setProgress(100, 0, true)
        .build()

    suspend fun createPaused(
        versionName: String,
        progress: Float,
    ): Notification {
        val percentInt = (progress * 100).toInt()
        return builder()
            .setContentTitle(getString(Res.string.notification_paused_title, versionName))
            .setContentText(getString(Res.string.notification_paused_message, percentInt))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    suspend fun createComplete(versionName: String): Notification = builder()
        .setContentTitle(getString(Res.string.notification_complete_title, versionName))
        .setContentText(getString(Res.string.notification_complete_message))
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    suspend fun createError(versionName: String): Notification = builder()
        .setContentTitle(getString(Res.string.notification_error_title, versionName))
        .setContentText(getString(Res.string.notification_error_message))
        .setSmallIcon(android.R.drawable.stat_notify_error)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    private suspend fun downloadingBuilder(versionName: String): NotificationCompat.Builder = builder()
        .setContentTitle(getString(Res.string.notification_preparing_title, versionName))
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)

    private fun builder(): NotificationCompat.Builder = NotificationCompat
        .Builder(context, CHANNEL_ID)
        .setContentIntent(tapPendingIntent())

    private fun tapPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AndroidBibleVersionDownloadNotifier.EXTRA_NAVIGATE_TO_BIBLE_VERSIONS, true)
        }
        return PendingIntent.getActivity(
            context,
            TAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        internal const val CHANNEL_ID = "bible_download_channel"
        private const val TAP_REQUEST_CODE = 1001
    }
}
