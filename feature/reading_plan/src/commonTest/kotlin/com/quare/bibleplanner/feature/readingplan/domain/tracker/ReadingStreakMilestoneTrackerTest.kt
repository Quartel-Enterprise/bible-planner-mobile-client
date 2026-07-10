package com.quare.bibleplanner.feature.readingplan.domain.tracker

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ReadingStreakMilestoneTrackerTest {
    private lateinit var tracker: ReadingStreakMilestoneTracker
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        trackedEvents = mutableListOf()
        tracker = ReadingStreakMilestoneTracker(
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }

    @Test
    fun `GIVEN a first emission at a threshold WHEN observed THEN fires nothing`() {
        // Given
        val restoredStreak = 7

        // When
        tracker.onStreak(restoredStreak)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a streak below 7 WHEN it reaches 7 THEN fires the 7 day milestone`() {
        // Given
        tracker.onStreak(6)

        // When
        tracker.onStreak(7)

        // Then
        assertEquals(
            listOf("reading_streak_milestone" to mapOf<String, Any>("streak_days" to 7)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN an unchanged streak WHEN observed repeatedly THEN fires only once`() {
        // Given
        tracker.onStreak(6)
        tracker.onStreak(7)
        trackedEvents.clear()

        // When
        tracker.onStreak(7)
        tracker.onStreak(7)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a broken streak WHEN a new streak reaches 7 again THEN fires the milestone again`() {
        // Given
        tracker.onStreak(6)
        tracker.onStreak(7)
        trackedEvents.clear()
        tracker.onStreak(0)

        // When
        tracker.onStreak(7)

        // Then
        assertEquals(
            listOf("reading_streak_milestone" to mapOf<String, Any>("streak_days" to 7)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a streak below 30 WHEN it jumps past 30 THEN fires only the missing thresholds`() {
        // Given
        tracker.onStreak(29)

        // When
        tracker.onStreak(31)

        // Then
        assertEquals(
            listOf("reading_streak_milestone" to mapOf<String, Any>("streak_days" to 30)),
            trackedEvents,
        )
    }
}
