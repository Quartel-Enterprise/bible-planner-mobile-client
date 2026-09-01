package com.quare.bibleplanner.feature.bibleversion.domain

interface BibleUpdatePromptPreferences {
    suspend fun getLastDismissedAt(): Long?

    suspend fun setLastDismissedAt(timestamp: Long)
}
