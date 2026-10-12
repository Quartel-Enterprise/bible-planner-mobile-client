package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.review.fake.FakeReviewPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal class ShouldRequestReviewUseCaseTest {
    private val grace: Duration = 3.days
    private val cooldown: Duration = 60.days

    @Test
    fun `GIVEN a first eligible timestamp within the grace period WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW - grace.inWholeMilliseconds + 1)

        // When
        val result = shouldRequestReview()

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN the grace period elapsed and no prompt yet WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW - grace.inWholeMilliseconds)

        // When
        val result = shouldRequestReview()

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN a first eligible timestamp in the future WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldRequestReview = useCase(firstEligibleAt = NOW + grace.inWholeMilliseconds)

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
            lastPromptedAt = NOW + cooldown.inWholeMilliseconds,
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
            lastPromptedAt = NOW - cooldown.inWholeMilliseconds + 1,
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
                lastPromptedAt = NOW - cooldown.inWholeMilliseconds,
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
        firstEligibleAt: Long? = NOW - grace.inWholeMilliseconds,
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
    }
}
