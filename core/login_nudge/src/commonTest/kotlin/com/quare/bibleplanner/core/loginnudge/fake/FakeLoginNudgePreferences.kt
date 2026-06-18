package com.quare.bibleplanner.core.loginnudge.fake

import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences

internal class FakeLoginNudgePreferences(
    private var snoozedAt: Long?,
    private var dontShowAgain: Boolean,
    private var firstActionAt: Long?,
) : LoginNudgePreferences {
    override suspend fun getSnoozedAt(): Long? = snoozedAt

    override suspend fun setSnoozedAt(timestamp: Long) {
        snoozedAt = timestamp
    }

    override suspend fun isDontShowAgain(): Boolean = dontShowAgain

    override suspend fun setDontShowAgain() {
        dontShowAgain = true
    }

    override suspend fun getFirstActionAt(): Long? = firstActionAt

    override suspend fun setFirstActionAt(timestamp: Long) {
        firstActionAt = timestamp
    }
}
