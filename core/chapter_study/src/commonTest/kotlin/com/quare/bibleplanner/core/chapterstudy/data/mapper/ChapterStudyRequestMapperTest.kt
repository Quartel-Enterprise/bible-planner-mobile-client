package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterStudyRequestMapperTest {
    private lateinit var mapper: ChapterStudyRequestMapper

    @BeforeTest
    fun setUp() {
        mapper = ChapterStudyRequestMapper(BookIdWireNameMapper())
    }

    @Test
    fun `GIVEN a chapter and a language WHEN mapping THEN builds the wire request with the full book name`() {
        // Given
        val chapter = ChapterRef(
            bibleVersionId = "ACF",
            bookId = BookId.SECOND_SA,
            chapterNumber = 7,
        )

        // When
        val request = mapper.map(
            chapter = chapter,
            languageCode = "pt-BR",
            isRewarded = false,
        )

        // Then
        assertEquals(
            expected = ChapterStudyRequestDto(
                book = "SECOND_SAMUEL",
                chapter = 7,
                version = "ACF",
                language = "pt-BR",
                reward = false,
            ),
            actual = request,
        )
    }
}
