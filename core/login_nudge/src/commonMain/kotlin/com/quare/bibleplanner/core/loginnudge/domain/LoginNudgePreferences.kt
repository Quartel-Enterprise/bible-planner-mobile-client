package com.quare.bibleplanner.core.loginnudge.domain

/**
 * Device-local persistence for the login nudge throttling. Not synced: the snooze and the
 * permanent dismissal are a per-device UX preference, not user data.
 */
interface LoginNudgePreferences {
    suspend fun getSnoozedAt(): Long?

    suspend fun setSnoozedAt(timestamp: Long)

    suspend fun isDontShowAgain(): Boolean

    suspend fun setDontShowAgain()

    /** Timestamp of the first nudge-eligible action, used for the post-install grace period. */
    suspend fun getFirstActionAt(): Long?

    suspend fun setFirstActionAt(timestamp: Long)
}
