package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.fake.dayStudyModel
import com.quare.bibleplanner.core.daystudy.fake.dayStudyResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyContentMapperTest {
    private lateinit var mapper: DayStudyContentMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyContentMapper()
    }

    @Test
    fun `GIVEN a study response WHEN mapping its content THEN keeps every section in order`() {
        // Given
        val content = dayStudyResponse(cacheToken = "token").content

        // When
        val study = mapper.map(content)

        // Then
        assertEquals(dayStudyModel, study)
    }
}
