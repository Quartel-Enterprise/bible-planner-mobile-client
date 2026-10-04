package com.quare.bibleplanner.core.loginnudge.domain

/*
 * Why: not synced on purpose; snooze and permanent dismissal are a per-device UX preference,
 * not user data.
 */
interface LoginNudgePreferences {
    suspend fun getSnoozedAt(): Long?

    suspend fun setSnoozedAt(timestamp: Long)

    suspend fun isDontShowAgain(): Boolean

    suspend fun setDontShowAgain()

    suspend fun getFirstActionAt(): Long?

    suspend fun setFirstActionAt(timestamp: Long)
}
