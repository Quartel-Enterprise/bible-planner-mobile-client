package com.quare.bibleplanner.feature.daystudy.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyGenerationUiModelTest {
    @Test
    fun `GIVEN the context step running WHEN reading the phases THEN earlier ones are done and context is active`() {
        // Given
        val generation = DayStudyGenerationUiModel(currentPhaseIndex = DayStudyGenerationPhase.CONTEXT.ordinal)

        // When
        val done = DayStudyGenerationPhase.entries.filter(generation::isDone)
        val active = DayStudyGenerationPhase.entries.filter(generation::isActive)

        // Then
        assertEquals(listOf(DayStudyGenerationPhase.READING, DayStudyGenerationPhase.CHAPTERS), done)
        assertEquals(listOf(DayStudyGenerationPhase.CONTEXT), active)
        assertEquals(DayStudyGenerationPhase.CONTEXT, generation.activePhase)
    }

    @Test
    fun `GIVEN every step finished WHEN reading the phases THEN all are done and the last one stays shown`() {
        // Given
        val generation = DayStudyGenerationUiModel(currentPhaseIndex = DayStudyGenerationPhase.entries.size)

        // When
        val done = DayStudyGenerationPhase.entries.filter(generation::isDone)

        // Then
        assertEquals(DayStudyGenerationPhase.entries, done)
        assertEquals(DayStudyGenerationPhase.QUESTIONS, generation.activePhase)
    }
}
