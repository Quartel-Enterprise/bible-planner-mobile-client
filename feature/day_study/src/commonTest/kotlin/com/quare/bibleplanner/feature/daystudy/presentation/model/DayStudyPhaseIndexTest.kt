package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import kotlin.test.Test
import kotlin.test.assertEquals

class DayStudyPhaseIndexTest {
    @Test
    fun `GIVEN every study phase WHEN mapping it THEN gets its generation phase index`() {
        // Given
        val phases = listOf(
            DayStudyPhaseModel.READING,
            DayStudyPhaseModel.CHAPTERS,
            DayStudyPhaseModel.CONTEXT,
            DayStudyPhaseModel.QUESTIONS,
        )

        // When
        val indexes = phases.map { it.toPhaseIndex() }

        // Then
        assertEquals(
            expected = listOf(
                DayStudyGenerationPhase.READING,
                DayStudyGenerationPhase.CHAPTERS,
                DayStudyGenerationPhase.CONTEXT,
                DayStudyGenerationPhase.QUESTIONS,
            ).map { it.ordinal },
            actual = indexes,
        )
    }
}
