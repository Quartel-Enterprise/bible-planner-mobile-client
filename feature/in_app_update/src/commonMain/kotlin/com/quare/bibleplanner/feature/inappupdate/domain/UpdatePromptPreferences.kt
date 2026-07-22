package com.quare.bibleplanner.feature.inappupdate.domain

interface UpdatePromptPreferences {
    suspend fun getLastPromptedAt(): Long?

    suspend fun setLastPromptedAt(timestamp: Long)
}
