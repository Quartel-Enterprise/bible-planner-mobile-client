package com.quare.bibleplanner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.notification_channel_description
import bibleplanner.composeapp.generated.resources.notification_channel_name
import bibleplanner.composeapp.generated.resources.notification_complete_message
import bibleplanner.composeapp.generated.resources.notification_complete_title
import bibleplanner.composeapp.generated.resources.notification_error_message
import bibleplanner.composeapp.generated.resources.notification_error_title
import bibleplanner.composeapp.generated.resources.notification_preparing_progress
import bibleplanner.composeapp.generated.resources.notification_preparing_starting
import bibleplanner.composeapp.generated.resources.notification_preparing_title
import org.jetbrains.compose.resources.getString

internal class BibleInitializationNotifier(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    private lateinit var channelName: String
    private lateinit var channelDescription: String
    private lateinit var preparingTitle: String
    private lateinit var preparingProgress: String
    private lateinit var preparingStarting: String
    private lateinit var completeTitle: String
    private lateinit var completeMessage: String
    private lateinit var errorTitle: String
    private lateinit var errorMessage: String

    suspend fun initialize() {
        channelName = getString(Res.string.notification_channel_name)
        channelDescription = getString(Res.string.notification_channel_description)
        preparingTitle = getString(Res.string.notification_preparing_title)
        preparingProgress = getString(Res.string.notification_preparing_progress)
        preparingStarting = getString(Res.string.notification_preparing_starting)
        completeTitle = getString(Res.string.notification_complete_title)
        completeMessage = getString(Res.string.notification_complete_message)
        errorTitle = getString(Res.string.notification_error_title)
        errorMessage = getString(Res.string.notification_error_message)

        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = channelDescription
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildProgressNotification(current: Int, total: Int) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(preparingTitle)
            .setContentText(
                if (total > 0) String.format(preparingProgress, current, total)
                else preparingStarting,
            )
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, current, total == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    fun showComplete() {
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(completeTitle)
            .setContentText(completeMessage)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showError() {
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(errorTitle)
            .setContentText(errorMessage)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "bible_initialization_channel"
        const val NOTIFICATION_ID = 1001
    }
}
