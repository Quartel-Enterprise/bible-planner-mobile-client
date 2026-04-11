package com.quare.bibleplanner.core.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AndroidNotificationBuilderProvider(
    private val context: Context,
    private val activityClass: Class<*>,
) : NotificationBuilderProvider {

    override fun getBaseBuilder(channelId: String, navigationExtraKey: String): NotificationCompat.Builder = NotificationCompat
        .Builder(context, channelId)
        .setContentIntent(tapPendingIntent(navigationExtraKey))

    private fun tapPendingIntent(navigationExtraKey: String): PendingIntent {
        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(navigationExtraKey, true)
        }
        return PendingIntent.getActivity(
            context,
            TAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAP_REQUEST_CODE = 1001
    }
}
