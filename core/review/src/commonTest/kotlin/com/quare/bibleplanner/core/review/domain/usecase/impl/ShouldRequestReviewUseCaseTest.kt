package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.review.fake.FakeReviewPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShouldRequestReviewUseCaseTest {
    @Test
    fun `GIVEN a first eligible timestamp within the grace period WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW - GRACE_MILLIS + 1)

        // When
        val result = shouldRequestReview()

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN the grace period elapsed and no prompt yet WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW - GRACE_MILLIS)

        // When
        val result = shouldRequestReview()

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a first eligible timestamp in the future WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW + GRACE_MILLIS)

        // When
        val result = shouldRequestReview()

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a last prompt timestamp in the future WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldRequestReview = useCase(
            lastPromptedVersion = "0.9.0",
            lastPromptedAt = NOW + COOLDOWN_MILLIS,
        )

        // When
        val result = shouldRequestReview()

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a prompt already shown on the current version WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldRequestReview = useCase(lastPromptedVersion = APP_VERSION)

        // When
        val result = shouldRequestReview()

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN a prompt on a previous version within the cooldown WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldRequestReview = useCase(
            lastPromptedVersion = "0.9.0",
            lastPromptedAt = NOW - COOLDOWN_MILLIS + 1,
        )

        // When
        val result = shouldRequestReview()

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN the cooldown elapsed after a prompt on a previous version WHEN evaluating THEN returns true`() =
        runTest {
            // Given
            val shouldRequestReview = useCase(
                lastPromptedVersion = "0.9.0",
                lastPromptedAt = NOW - COOLDOWN_MILLIS,
            )

            // When
            val result = shouldRequestReview()

            // Then
            assertTrue(result)
        }

    @Test
    fun `GIVEN no first eligible timestamp WHEN evaluating THEN stamps it and suppresses the first prompt`() = runTest {
        // Given
        val preferences = FakeReviewPreferences(
            firstEligibleAt = null,
            lastPromptedAt = null,
            lastPromptedVersion = null,
        )
        val shouldRequestReview = useCase(preferences)

        // When
        val result = shouldRequestReview()

        // Then
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
        hasCooldownElapsed = HasCooldownElapsedUseCase { NOW },
        appVersion = APP_VERSION,
    )

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val APP_VERSION = "1.0.0"
        const val GRACE_MILLIS = 3L * 24 * 60 * 60 * 1000
        const val COOLDOWN_MILLIS = 60L * 24 * 60 * 60 * 1000
    }
}
