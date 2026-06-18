package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import com.quare.bibleplanner.core.loginnudge.domain.usecase.ShouldShowLoginNudge
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

internal class ShouldShowLoginNudgeUseCase(
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val loginNudgePreferences: LoginNudgePreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ShouldShowLoginNudge {
    private val snoozeDuration: Duration = 24.hours
    private val firstActionGracePeriod: Duration = 12.hours

    override suspend fun invoke(): Boolean {
        if (getAuthenticatedUserId() != null) return false
        if (loginNudgePreferences.isDontShowAgain()) return false
        val now = currentTimestampProvider.getCurrentTimestamp()
        if (isWithinFirstActionGracePeriod(now)) return false
        if (!isConnected()) return false
        val snoozedAt = loginNudgePreferences.getSnoozedAt() ?: return true
        return (now - snoozedAt).milliseconds >= snoozeDuration
    }

    private suspend fun isWithinFirstActionGracePeriod(now: Long): Boolean {
        val firstActionAt = loginNudgePreferences.getFirstActionAt() ?: run {
            loginNudgePreferences.setFirstActionAt(now)
            now
        }
        return (now - firstActionAt).milliseconds < firstActionGracePeriod
    }
}
