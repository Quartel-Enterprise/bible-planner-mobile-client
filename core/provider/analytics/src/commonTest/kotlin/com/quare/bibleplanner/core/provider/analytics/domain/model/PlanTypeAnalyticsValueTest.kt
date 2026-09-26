package com.quare.bibleplanner.core.provider.analytics.domain.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlin.test.Test
import kotlin.test.assertEquals

class PlanTypeAnalyticsValueTest {
    @Test
    fun `GIVEN every plan type WHEN converting to an analytics value THEN uses its lowercase name`() {
        // When
        val values = ReadingPlanType.entries.map { it.toAnalyticsValue() }

        // Then
        assertEquals(
            expected = listOf("chronological", "books"),
            actual = values,
        )
    }
}
