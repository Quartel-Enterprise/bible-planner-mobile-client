package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyPhaseMapperTest {
    private lateinit var mapper: DayStudyPhaseMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyPhaseMapper()
    }

    @Test
    fun `GIVEN the known wire phases WHEN mapping them THEN returns the matching models`() {
        // Given
        val wirePhases = listOf("reading", "chapters", "context", "questions")

        // When
        val models = wirePhases.map(mapper::map)

        // Then
        assertEquals(
            expected = listOf(
                DayStudyPhaseModel.READING,
                DayStudyPhaseModel.CHAPTERS,
                DayStudyPhaseModel.CONTEXT,
                DayStudyPhaseModel.QUESTIONS,
            ),
            actual = models,
        )
    }

    @Test
    fun `GIVEN an unknown and a blank wire phase WHEN mapping them THEN returns null for each`() {
        // Given
        val wirePhases = listOf("polishing", "")

        // When
        val models = wirePhases.map(mapper::map)

        // Then
        assertEquals(
            expected = listOf(null, null),
            actual = models,
        )
    }
}
