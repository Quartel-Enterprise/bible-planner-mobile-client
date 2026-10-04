package com.quare.bibleplanner.core.model.plan

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadingPlanTypeTest {
    @Test
    fun `GIVEN the reading plan types WHEN listing their names THEN keeps the names persisted in the days table`() {
        // Given
        val planTypes = ReadingPlanType.entries

        // When
        val names = planTypes.map { it.name }

        // Then
        assertEquals(listOf("CHRONOLOGICAL", "BOOKS"), names)
    }
}
