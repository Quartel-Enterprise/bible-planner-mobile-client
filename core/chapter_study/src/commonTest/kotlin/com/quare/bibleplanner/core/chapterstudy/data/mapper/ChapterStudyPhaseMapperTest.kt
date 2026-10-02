package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChapterStudyPhaseMapperTest {
    private lateinit var mapper: ChapterStudyPhaseMapper

    @BeforeTest
    fun setUp() {
        mapper = ChapterStudyPhaseMapper()
    }

    @Test
    fun `GIVEN the reading wire phase WHEN mapping THEN returns READING`() {
        // When
        val phase = mapper.mapOrNull("reading")

        // Then
        assertEquals(
            expected = ChapterStudyPhaseModel.READING,
            actual = phase,
        )
    }

    @Test
    fun `GIVEN the summary wire phase WHEN mapping THEN returns SUMMARY`() {
        // When
        val phase = mapper.mapOrNull("summary")

        // Then
        assertEquals(
            expected = ChapterStudyPhaseModel.SUMMARY,
            actual = phase,
        )
    }

    @Test
    fun `GIVEN the context wire phase WHEN mapping THEN returns CONTEXT`() {
        // When
        val phase = mapper.mapOrNull("context")

        // Then
        assertEquals(
            expected = ChapterStudyPhaseModel.CONTEXT,
            actual = phase,
        )
    }

    @Test
    fun `GIVEN the questions wire phase WHEN mapping THEN returns QUESTIONS`() {
        // When
        val phase = mapper.mapOrNull("questions")

        // Then
        assertEquals(
            expected = ChapterStudyPhaseModel.QUESTIONS,
            actual = phase,
        )
    }

    @Test
    fun `GIVEN an unknown wire phase WHEN mapping THEN returns null`() {
        // When & Then
        assertNull(mapper.mapOrNull("polishing"))
        assertNull(mapper.mapOrNull("READING"))
        assertNull(mapper.mapOrNull(""))
    }
}
