package com.quare.bibleplanner.notification

import android.content.Context
import com.quare.bibleplanner.R

internal class AndroidNotificationStringProvider(
    private val context: Context,
) : NotificationStringProvider {
    override fun getPreparingTitle(versionName: String): String =
        context.getString(R.string.notification_preparing_title, versionName)

    override fun getPreparingProgress(percent: Int): String =
        context.getString(R.string.notification_preparing_progress, percent)

    override fun getPreparingStarting(): String = context.getString(R.string.notification_preparing_starting)

    override fun getCompleteTitle(versionName: String): String =
        context.getString(R.string.notification_complete_title, versionName)

    override fun getCompleteMessage(): String = context.getString(R.string.notification_complete_message)

    override fun getErrorTitle(versionName: String): String =
        context.getString(R.string.notification_error_title, versionName)

    override fun getErrorMessage(): String = context.getString(R.string.notification_error_message)

    override fun getPausedTitle(versionName: String): String =
        context.getString(R.string.notification_paused_title, versionName)

    override fun getPausedMessage(percent: Int): String =
        context.getString(R.string.notification_paused_message, percent)
}
