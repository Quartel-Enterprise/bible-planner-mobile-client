package com.quare.bibleplanner.feature.readingplan.domain.tracker

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

internal class BibleProgressMilestoneTracker(
    private val trackEvent: TrackEvent,
) {
    private val thresholds = listOf(25, 50, 75, 100)
    private var highestReachedThreshold: Int? = null

    fun onProgress(percent: Float) {
        val reached = thresholds.lastOrNull { percent >= it } ?: 0
        val previous = highestReachedThreshold
        highestReachedThreshold = when {
            previous == null || percent == 0f -> {
                reached
            }

            reached > previous -> {
                thresholds
                    .filter { it > previous && it <= reached }
                    .forEach(::trackMilestone)
                reached
            }

            else -> {
                previous
            }
        }
    }

    private fun trackMilestone(percent: Int) {
        trackEvent(
            name = AnalyticsEventNames.BIBLE_PROGRESS_MILESTONE,
            params = mapOf(AnalyticsParams.PERCENT to percent),
        )
    }
}
