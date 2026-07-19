package com.quare.bibleplanner.core.review.domain

interface ReviewPreferences {
    suspend fun getFirstEligibleAt(): Long?

    suspend fun setFirstEligibleAt(timestamp: Long)

    suspend fun getLastPromptedAt(): Long?

    suspend fun setLastPromptedAt(timestamp: Long)

    suspend fun getLastPromptedVersion(): String?

    suspend fun setLastPromptedVersion(version: String)
}
