package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.review.fake.FakeReviewPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShouldRequestReviewUseCaseTest {
    @Test
    fun `returns false within the first eligible grace period`() = runTest {
        assertFalse(useCase(firstEligibleAt = NOW - GRACE_MILLIS + 1)())
    }

    @Test
    fun `returns true once the grace period elapsed and never prompted`() = runTest {
        assertTrue(useCase(firstEligibleAt = NOW - GRACE_MILLIS)())
    }

    @Test
    fun `returns false when already prompted on the current version`() = runTest {
        assertFalse(useCase(lastPromptedVersion = APP_VERSION)())
    }

    @Test
    fun `returns false within the cooldown after a prompt on a previous version`() = runTest {
        assertFalse(
            useCase(
                lastPromptedVersion = "0.9.0",
                lastPromptedAt = NOW - COOLDOWN_MILLIS + 1,
            )(),
        )
    }

    @Test
    fun `returns true once the cooldown elapsed on a previous version`() = runTest {
        assertTrue(
            useCase(
                lastPromptedVersion = "0.9.0",
                lastPromptedAt = NOW - COOLDOWN_MILLIS,
            )(),
        )
    }

    @Test
    fun `stamps the first eligible timestamp and suppresses on the very first evaluation`() = runTest {
        val preferences = FakeReviewPreferences(
            firstEligibleAt = null,
            lastPromptedAt = null,
            lastPromptedVersion = null,
        )

        val result = useCase(preferences)()

        assertFalse(result)
        assertEquals(NOW, preferences.getFirstEligibleAt())
    }

    private fun useCase(
        firstEligibleAt: Long? = NOW - GRACE_MILLIS,
        lastPromptedAt: Long? = null,
        lastPromptedVersion: String? = null,
    ): ShouldRequestReviewUseCase = useCase(
        preferences = FakeReviewPreferences(
            firstEligibleAt = firstEligibleAt,
            lastPromptedAt = lastPromptedAt,
            lastPromptedVersion = lastPromptedVersion,
        ),
    )

    private fun useCase(preferences: FakeReviewPreferences): ShouldRequestReviewUseCase = ShouldRequestReviewUseCase(
        reviewPreferences = preferences,
        currentTimestampProvider = { NOW },
        appVersion = APP_VERSION,
    )

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val APP_VERSION = "1.0.0"
        const val GRACE_MILLIS = 3L * 24 * 60 * 60 * 1000
        const val COOLDOWN_MILLIS = 60L * 24 * 60 * 60 * 1000
    }
}
