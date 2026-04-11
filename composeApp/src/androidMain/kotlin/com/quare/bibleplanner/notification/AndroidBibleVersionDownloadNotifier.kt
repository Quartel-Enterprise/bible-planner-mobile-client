package com.quare.bibleplanner.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier

internal class AndroidBibleVersionDownloadNotifier(
    private val context: Context,
    private val notificationFactory: BibleVersionNotificationFactory,
) : BibleVersionDownloadNotifier {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        val channel = NotificationChannel(
            BibleVersionNotificationFactory.CHANNEL_ID,
            "Bible Download",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        if (!canShowNotification()) return
        notificationManager.notify(
            getNotificationId(versionId),
            notificationFactory.createProgress(versionName, progress),
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        if (!canShowNotification()) return
        notificationManager.cancel(getPausedNotificationId(versionId))
        notificationManager.notify(getResultNotificationId(versionId), notificationFactory.createComplete(versionName))
    }

    @SuppressLint("MissingPermission")
    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        if (!canShowNotification()) return
        notificationManager.notify(getResultNotificationId(versionId), notificationFactory.createError(versionName))
    }

    @SuppressLint("MissingPermission")
    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        if (!canShowNotification()) return
        notificationManager.notify(
            getPausedNotificationId(versionId),
            notificationFactory.createPaused(versionName, progress),
        )
    }

    override suspend fun dismiss(versionId: String) {
        notificationManager.cancel(getNotificationId(versionId))
        notificationManager.cancel(getResultNotificationId(versionId))
        notificationManager.cancel(getPausedNotificationId(versionId))
    }

    internal fun buildForegroundInfo(
        versionId: String,
        versionName: String,
    ): ForegroundInfo {
        val notification = notificationFactory.createForeground(versionName)
        val notificationId = getNotificationId(versionId)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun canShowNotification(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        notificationManager.areNotificationsEnabled()
    }

    private fun getNotificationId(versionId: String): Int = versionId.hashCode()

    private fun getResultNotificationId(versionId: String): Int = "result_$versionId".hashCode()

    private fun getPausedNotificationId(versionId: String): Int = "paused_$versionId".hashCode()

    companion object {
        const val EXTRA_NAVIGATE_TO_BIBLE_VERSIONS = "navigate_to_bible_versions"
    }
}
