package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningSegmentMapperTest {
    @Test
    fun `GIVEN a chapter WHEN mapping it to a segment THEN the segment covers the whole chapter`() {
        // Given
        val chapter = ChapterLocationModel(
            bookId = BookId.PSA,
            chapterNumber = 23,
        )

        // When
        val segment = chapter.toWholeChapterSegment()

        // Then
        assertEquals(
            ListeningSegmentModel(
                chapter = chapter,
                startVerse = null,
                endVerse = null,
            ),
            segment,
        )
    }
}
