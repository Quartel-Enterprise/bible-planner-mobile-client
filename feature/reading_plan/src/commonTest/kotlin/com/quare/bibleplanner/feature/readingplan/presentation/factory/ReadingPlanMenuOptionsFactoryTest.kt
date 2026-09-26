package com.quare.bibleplanner.feature.readingplan.presentation.factory

import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOptionPresentationModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadingPlanMenuOptionsFactoryTest {
    @Test
    fun `GIVEN the overflow menu WHEN listing its options THEN offers the start day before deleting progress`() {
        // Given
        val factory = ReadingPlanMenuOptionsFactory

        // When
        val options = factory.options

        // Then
        assertEquals(
            expected = listOf(OverflowOption.EDIT_START_DAY, OverflowOption.DELETE_PROGRESS),
            actual = options.map(OverflowOptionPresentationModel::type),
        )
    }
}
