package com.quare.bibleplanner.core.provider.analytics.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class EventAnalyticsTest {
    @Test
    fun `GIVEN a single event name WHEN declaring a manual decision THEN it covers exactly that event`() {
        // When
        val decision = EventAnalytics.Track.Manual(AnalyticsEventNames.SYNC_COMPLETED)

        // Then
        assertEquals(
            expected = EventAnalytics.Track.Manual(setOf(AnalyticsEventNames.SYNC_COMPLETED)),
            actual = decision,
        )
    }

    @Test
    fun `GIVEN an automatic decision WHEN reading it THEN keeps its name and params`() {
        // When
        val decision = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SYNC_FAILED,
            params = mapOf(AnalyticsParams.SOURCE to "startup"),
        )

        // Then
        assertEquals(
            expected = AnalyticsEventNames.SYNC_FAILED,
            actual = decision.name,
        )
        assertEquals(
            expected = mapOf<String, Any>(AnalyticsParams.SOURCE to "startup"),
            actual = decision.params,
        )
    }
}
