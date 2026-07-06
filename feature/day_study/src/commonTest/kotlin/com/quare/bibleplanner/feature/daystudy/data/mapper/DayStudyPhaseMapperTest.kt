package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DayStudyPhaseMapperTest {
    private lateinit var mapper: DayStudyPhaseMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyPhaseMapper()
    }

    @Test
    fun `WHEN mapping known wire phases THEN returns the matching model`() {
        // When & Then
        assertEquals(DayStudyPhaseModel.READING, mapper.map("reading"))
        assertEquals(DayStudyPhaseModel.CHAPTERS, mapper.map("chapters"))
        assertEquals(DayStudyPhaseModel.CONTEXT, mapper.map("context"))
        assertEquals(DayStudyPhaseModel.QUESTIONS, mapper.map("questions"))
    }

    @Test
    fun `WHEN mapping an unknown wire phase THEN returns null`() {
        // When & Then
        assertNull(mapper.map("polishing"))
        assertNull(mapper.map(""))
    }
}
