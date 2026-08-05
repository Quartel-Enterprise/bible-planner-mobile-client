package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import com.quare.bibleplanner.core.loginnudge.domain.usecase.ShouldShowLoginNudge
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

internal class ShouldShowLoginNudgeUseCase(
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val isConnected: IsConnected,
    private val loginNudgePreferences: LoginNudgePreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val hasCooldownElapsed: HasCooldownElapsedUseCase,
) : ShouldShowLoginNudge {
    private val snoozeDuration: Duration = 24.hours
    private val firstActionGracePeriod: Duration = 12.hours

    override suspend fun invoke(): Boolean {
        if (getAuthenticatedUserId() != null) return false
        if (loginNudgePreferences.isDontShowAgain()) return false
        if (isWithinFirstActionGracePeriod()) return false
        if (!isConnected()) return false
        return hasCooldownElapsed(
            lastOccurredAt = loginNudgePreferences.getSnoozedAt(),
            cooldown = snoozeDuration,
        )
    }

    private suspend fun isWithinFirstActionGracePeriod(): Boolean {
        val firstActionAt = loginNudgePreferences.getFirstActionAt() ?: run {
            val now = currentTimestampProvider.getCurrentTimestamp()
            loginNudgePreferences.setFirstActionAt(now)
            now
        }
        return !hasCooldownElapsed(
            lastOccurredAt = firstActionAt,
            cooldown = firstActionGracePeriod,
        )
    }
}
