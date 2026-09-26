package com.quare.bibleplanner.core.model.plan

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadingPlanTypeTest {
    @Test
    fun `WHEN listing the plan types THEN keeps the names persisted in the days table`() {
        // When
        val names = ReadingPlanType.entries.map { it.name }

        // Then
        assertEquals(listOf("CHRONOLOGICAL", "BOOKS"), names)
    }
}
