package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.review.domain.ReviewPreferences
import com.quare.bibleplanner.core.review.domain.usecase.ShouldRequestReview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal class ShouldRequestReviewUseCase(
    private val reviewPreferences: ReviewPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val hasCooldownElapsed: HasCooldownElapsedUseCase,
    private val appVersion: String,
) : ShouldRequestReview {
    private val firstEligibleGracePeriod: Duration = 3.days
    private val promptCooldown: Duration = 60.days

    override suspend fun invoke(): Boolean {
        if (isWithinGracePeriod()) return false
        if (reviewPreferences.getLastPromptedVersion() == appVersion) return false
        return hasCooldownElapsed(
            lastOccurredAt = reviewPreferences.getLastPromptedAt(),
            cooldown = promptCooldown,
        )
    }

    private suspend fun isWithinGracePeriod(): Boolean {
        val firstEligibleAt = reviewPreferences.getFirstEligibleAt() ?: run {
            val now = currentTimestampProvider.getCurrentTimestamp()
            reviewPreferences.setFirstEligibleAt(now)
            now
        }
        return !hasCooldownElapsed(
            lastOccurredAt = firstEligibleAt,
            cooldown = firstEligibleGracePeriod,
        )
    }
}
