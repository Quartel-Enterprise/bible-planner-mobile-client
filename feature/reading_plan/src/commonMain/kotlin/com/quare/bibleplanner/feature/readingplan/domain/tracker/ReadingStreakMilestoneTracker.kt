package com.quare.bibleplanner.feature.readingplan.domain.tracker

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

internal class ReadingStreakMilestoneTracker(
    private val trackEvent: TrackEvent,
) {
    private val thresholds = listOf(7, 30, 100, 365)
    private var previousStreak: Int? = null
    private var highestFiredThreshold = 0

    fun onStreak(streakDays: Int): Boolean {
        val previous = previousStreak
        previousStreak = streakDays
        if (previous == null || streakDays < previous) {
            highestFiredThreshold = thresholds.lastOrNull { streakDays >= it } ?: 0
            return false
        }
        val crossed = thresholds.filter { it > highestFiredThreshold && streakDays >= it }
        crossed.forEach { threshold ->
            trackEvent(
                name = AnalyticsEventNames.READING_STREAK_MILESTONE,
                params = mapOf(AnalyticsParams.STREAK_DAYS to threshold),
            )
            highestFiredThreshold = threshold
        }
        return crossed.isNotEmpty()
    }
}
