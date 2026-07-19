package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.review.domain.ReviewPreferences
import com.quare.bibleplanner.core.review.domain.usecase.ShouldRequestReview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

internal class ShouldRequestReviewUseCase(
    private val reviewPreferences: ReviewPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val appVersion: String,
) : ShouldRequestReview {
    private val firstEligibleGracePeriod: Duration = 3.days
    private val promptCooldown: Duration = 60.days

    override suspend fun invoke(): Boolean {
        val now = currentTimestampProvider.getCurrentTimestamp()
        if (isWithinGracePeriod(now)) return false
        if (reviewPreferences.getLastPromptedVersion() == appVersion) return false
        val lastPromptedAt = reviewPreferences.getLastPromptedAt() ?: return true
        return (now - lastPromptedAt).milliseconds >= promptCooldown
    }

    private suspend fun isWithinGracePeriod(now: Long): Boolean {
        val firstEligibleAt = reviewPreferences.getFirstEligibleAt() ?: run {
            reviewPreferences.setFirstEligibleAt(now)
            now
        }
        return (now - firstEligibleAt).milliseconds < firstEligibleGracePeriod
    }
}
