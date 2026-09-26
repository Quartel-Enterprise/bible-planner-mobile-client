package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyStatusMapperTest {
    private lateinit var mapper: DayStudyStatusMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyStatusMapper()
    }

    @Test
    fun `GIVEN a status response WHEN mapping THEN keeps the quota and the unlock and cache token`() {
        // Given
        val dto = DayStudyStatusDto(
            isUnlocked = true,
            usedCount = 2,
            freeLimit = 3,
            isPro = false,
            clientCacheToken = "token",
        )

        // When
        val status = mapper.map(dto)

        // Then
        assertEquals(
            DayStudyStatusModel(
                freeLimit = 3,
                usedCount = 2,
                isUnlocked = true,
                cacheToken = "token",
            ),
            status,
        )
    }
}
