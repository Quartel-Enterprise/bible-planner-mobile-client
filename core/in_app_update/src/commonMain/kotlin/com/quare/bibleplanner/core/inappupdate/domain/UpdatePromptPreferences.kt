package com.quare.bibleplanner.core.inappupdate.domain

interface UpdatePromptPreferences {
    suspend fun getLastPromptedAt(): Long?

    suspend fun setLastPromptedAt(timestamp: Long)
}
