package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterStudyStatusMapperTest {
    private lateinit var mapper: ChapterStudyStatusMapper

    @BeforeTest
    fun setUp() {
        mapper = ChapterStudyStatusMapper()
    }

    @Test
    fun `GIVEN a status response WHEN mapping THEN keeps the quota and the unlock and cache token`() {
        // Given
        val dto = ChapterStudyStatusDto(
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
            expected = ChapterStudyStatusModel(
                freeLimit = 3,
                usedCount = 2,
                isUnlocked = true,
                cacheToken = "token",
            ),
            actual = status,
        )
    }
}
