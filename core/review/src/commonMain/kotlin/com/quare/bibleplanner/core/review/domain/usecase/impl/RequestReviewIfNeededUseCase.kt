package com.quare.bibleplanner.core.review.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import com.quare.bibleplanner.core.review.domain.ReviewPreferences
import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger
import com.quare.bibleplanner.core.review.domain.usecase.RequestReviewIfNeeded
import com.quare.bibleplanner.core.review.domain.usecase.ShouldRequestReview

internal class RequestReviewIfNeededUseCase(
    private val shouldRequestReview: ShouldRequestReview,
    private val requestInAppReview: RequestInAppReview,
    private val reviewPreferences: ReviewPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val trackEvent: TrackEvent,
    private val appVersion: String,
) : RequestReviewIfNeeded {
    override suspend fun invoke(trigger: ReviewTrigger) {
        if (!shouldRequestReview()) return
        if (!requestInAppReview()) return
        reviewPreferences.setLastPromptedVersion(appVersion)
        reviewPreferences.setLastPromptedAt(currentTimestampProvider.getCurrentTimestamp())
        trackEvent(
            name = AnalyticsEventNames.IN_APP_REVIEW_REQUESTED,
            params = mapOf(AnalyticsParams.TRIGGER to trigger.analyticsValue),
        )
    }
}
