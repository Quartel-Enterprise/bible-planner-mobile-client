package com.quare.bibleplanner.core.notification

import androidx.core.app.NotificationCompat

interface NotificationBuilderProvider {
    fun getBaseBuilder(
        channelId: String,
        navigationExtraKey: String,
    ): NotificationCompat.Builder
}
