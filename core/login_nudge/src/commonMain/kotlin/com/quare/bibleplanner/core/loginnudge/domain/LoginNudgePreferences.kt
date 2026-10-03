package com.quare.bibleplanner.core.loginnudge.domain

interface LoginNudgePreferences {
    suspend fun getSnoozedAt(): Long?

    suspend fun setSnoozedAt(timestamp: Long)

    suspend fun isDontShowAgain(): Boolean

    suspend fun setDontShowAgain()

    suspend fun getFirstActionAt(): Long?

    suspend fun setFirstActionAt(timestamp: Long)
}
