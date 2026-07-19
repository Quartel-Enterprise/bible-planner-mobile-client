package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger
import com.quare.bibleplanner.core.review.fake.FakeReviewPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RequestReviewIfNeededUseCaseTest {
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @Test
    fun `does nothing when the policy rejects the request`() = runTest {
        val preferences = preferences()

        useCase(shouldRequest = false, preferences = preferences)(ReviewTrigger.STREAK_MILESTONE)

        assertNull(preferences.getLastPromptedVersion())
        assertNull(preferences.getLastPromptedAt())
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `does not record or track when the native review flow could not launch`() = runTest {
        val preferences = preferences()

        useCase(reviewLaunched = false, preferences = preferences)(ReviewTrigger.PROGRESS_MILESTONE)

        assertNull(preferences.getLastPromptedVersion())
        assertNull(preferences.getLastPromptedAt())
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `records the prompt and tracks the trigger when the review flow launches`() = runTest {
        val preferences = preferences()

        useCase(preferences = preferences)(ReviewTrigger.BOOK_COMPLETED)

        assertEquals(APP_VERSION, preferences.getLastPromptedVersion())
        assertEquals(NOW, preferences.getLastPromptedAt())
        assertEquals(
            listOf(
                AnalyticsEventNames.IN_APP_REVIEW_REQUESTED to mapOf<String, Any>(
                    AnalyticsParams.TRIGGER to ReviewTrigger.BOOK_COMPLETED.analyticsValue,
                ),
            ),
            trackedEvents,
        )
    }

    private fun preferences(): FakeReviewPreferences = FakeReviewPreferences(
        firstEligibleAt = NOW,
        lastPromptedAt = null,
        lastPromptedVersion = null,
    )

    private fun useCase(
        shouldRequest: Boolean = true,
        reviewLaunched: Boolean = true,
        preferences: FakeReviewPreferences,
    ): RequestReviewIfNeededUseCase = RequestReviewIfNeededUseCase(
        shouldRequestReview = { shouldRequest },
        requestInAppReview = { reviewLaunched },
        reviewPreferences = preferences,
        currentTimestampProvider = { NOW },
        trackEvent = { name, params -> trackedEvents += name to params },
        appVersion = APP_VERSION,
    )

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val APP_VERSION = "1.0.0"
    }
}
