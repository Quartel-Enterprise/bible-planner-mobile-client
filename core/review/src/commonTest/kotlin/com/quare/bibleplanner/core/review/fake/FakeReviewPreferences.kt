package com.quare.bibleplanner.core.review.fake

import com.quare.bibleplanner.core.review.domain.ReviewPreferences

internal class FakeReviewPreferences(
    private var firstEligibleAt: Long?,
    private var lastPromptedAt: Long?,
    private var lastPromptedVersion: String?,
) : ReviewPreferences {
    override suspend fun getFirstEligibleAt(): Long? = firstEligibleAt

    override suspend fun setFirstEligibleAt(timestamp: Long) {
        firstEligibleAt = timestamp
    }

    override suspend fun getLastPromptedAt(): Long? = lastPromptedAt

    override suspend fun setLastPromptedAt(timestamp: Long) {
        lastPromptedAt = timestamp
    }

    override suspend fun getLastPromptedVersion(): String? = lastPromptedVersion

    override suspend fun setLastPromptedVersion(version: String) {
        lastPromptedVersion = version
    }
}
