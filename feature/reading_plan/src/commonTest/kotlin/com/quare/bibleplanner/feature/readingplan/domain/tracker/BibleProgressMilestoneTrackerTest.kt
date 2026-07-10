package com.quare.bibleplanner.feature.readingplan.domain.tracker

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BibleProgressMilestoneTrackerTest {
    private lateinit var tracker: BibleProgressMilestoneTracker
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        trackedEvents = mutableListOf()
        tracker = BibleProgressMilestoneTracker(
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }

    @Test
    fun `GIVEN a first emission above a threshold WHEN observed THEN fires nothing`() {
        // Given
        val initialProgress = 60f

        // When
        tracker.onProgress(initialProgress)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a baseline below 25 WHEN progress crosses 25 THEN fires the 25 milestone`() {
        // Given
        tracker.onProgress(10f)

        // When
        tracker.onProgress(26f)

        // Then
        assertEquals(
            listOf("bible_progress_milestone" to mapOf<String, Any>("percent" to 25)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a baseline below 25 WHEN a bulk action crosses several thresholds THEN fires one event per threshold`() {
        // Given
        tracker.onProgress(10f)

        // When
        tracker.onProgress(80f)

        // Then
        assertEquals(
            listOf(
                "bible_progress_milestone" to mapOf<String, Any>("percent" to 25),
                "bible_progress_milestone" to mapOf<String, Any>("percent" to 50),
                "bible_progress_milestone" to mapOf<String, Any>("percent" to 75),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a crossed threshold WHEN progress drops and re-crosses it THEN does not re-fire`() {
        // Given
        tracker.onProgress(10f)
        tracker.onProgress(30f)
        trackedEvents.clear()

        // When
        tracker.onProgress(20f)
        tracker.onProgress(30f)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a progress reset to zero WHEN thresholds are crossed again THEN fires them again`() {
        // Given
        tracker.onProgress(10f)
        tracker.onProgress(30f)
        trackedEvents.clear()
        tracker.onProgress(0f)

        // When
        tracker.onProgress(26f)

        // Then
        assertEquals(
            listOf("bible_progress_milestone" to mapOf<String, Any>("percent" to 25)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN progress just below completion WHEN it reaches 100 THEN fires the 100 milestone`() {
        // Given
        tracker.onProgress(90f)

        // When
        tracker.onProgress(100f)

        // Then
        assertEquals(
            listOf("bible_progress_milestone" to mapOf<String, Any>("percent" to 100)),
            trackedEvents,
        )
    }
}
