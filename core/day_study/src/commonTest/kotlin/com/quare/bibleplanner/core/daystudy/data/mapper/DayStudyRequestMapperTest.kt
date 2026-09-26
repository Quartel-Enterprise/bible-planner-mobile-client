package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.PassageRequestDto
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyRequestMapperTest {
    private lateinit var mapper: DayStudyRequestMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyRequestMapper(BookIdWireNameMapper())
    }

    @Test
    fun `GIVEN passages with whole chapters and verse ranges WHEN mapping THEN builds the wire request`() {
        // Given
        val passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(
                    ChapterModel(
                        number = 1,
                        startVerse = null,
                        endVerse = null,
                        bookId = BookId.GEN,
                    ),
                    ChapterModel(
                        number = 2,
                        startVerse = 4,
                        endVerse = 9,
                        bookId = BookId.GEN,
                    ),
                ),
                isRead = false,
                chapterRanges = "1-2",
            ),
            PassageModel(
                bookId = BookId.EXO,
                chapters = emptyList(),
                isRead = false,
                chapterRanges = null,
            ),
        )

        // When
        val request = mapper.map(
            passages = passages,
            version = "ACF",
            languageCode = "pt-BR",
        )

        // Then
        assertEquals(
            DayStudyRequestDto(
                passages = listOf(
                    PassageRequestDto(
                        book = "GENESIS",
                        chapters = listOf(
                            ChapterRequestDto(
                                number = 1,
                                startVerse = null,
                                endVerse = null,
                            ),
                            ChapterRequestDto(
                                number = 2,
                                startVerse = 4,
                                endVerse = 9,
                            ),
                        ),
                    ),
                    PassageRequestDto(
                        book = "EXODUS",
                        chapters = emptyList(),
                    ),
                ),
                version = "ACF",
                language = "pt-BR",
            ),
            request,
        )
    }
}
