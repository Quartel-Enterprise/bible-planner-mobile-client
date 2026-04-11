package com.quare.bibleplanner.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.quare.bibleplanner.core.notification.NotificationBuilderProvider

internal class BibleVersionNotificationFactory(
    private val stringProvider: NotificationStringProvider,
    private val builderProvider: NotificationBuilderProvider,
) {
    fun createProgress(
        versionName: String,
        progress: Float,
    ): Notification {
        val percentInt = (progress * 100).toInt()
        return getDownloadingBuilder(versionName)
            .setContentText(
                if (percentInt > 0) {
                    stringProvider.getPreparingProgress(percentInt)
                } else {
                    stringProvider.getPreparingStarting()
                },
            ).setProgress(100, percentInt, percentInt == 0)
            .build()
    }

    fun createForeground(versionName: String): Notification = getDownloadingBuilder(versionName)
        .setProgress(100, 0, true)
        .build()

    fun createPaused(
        versionName: String,
        progress: Float,
    ): Notification = getBaseBuilder()
        .setContentTitle(stringProvider.getPausedTitle(versionName))
        .setContentText(stringProvider.getPausedMessage((progress * 100).toInt()))
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    fun createComplete(versionName: String): Notification = buildResultNotification(
        title = stringProvider.getCompleteTitle(versionName),
        message = stringProvider.getCompleteMessage(),
        icon = android.R.drawable.stat_sys_download_done,
    )

    fun createError(versionName: String): Notification = buildResultNotification(
        title = stringProvider.getErrorTitle(versionName),
        message = stringProvider.getErrorMessage(),
        icon = android.R.drawable.stat_notify_error,
    )

    private fun buildResultNotification(title: String, message: String, icon: Int): Notification =
        getBaseBuilder()
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

    private fun getDownloadingBuilder(versionName: String): NotificationCompat.Builder = getBaseBuilder()
        .setContentTitle(stringProvider.getPreparingTitle(versionName))
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)

    private fun getBaseBuilder(): NotificationCompat.Builder =
        builderProvider.getBaseBuilder(CHANNEL_ID, AndroidBibleVersionDownloadNotifier.EXTRA_NAVIGATE_TO_BIBLE_VERSIONS)

    companion object {
        internal const val CHANNEL_ID = "bible_download_channel"
    }
}
