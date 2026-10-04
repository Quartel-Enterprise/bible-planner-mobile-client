package com.quare.bibleplanner.feature.read.presentation.screen.content

import com.quare.bibleplanner.feature.read.fixture.readChapter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterStartIndicesTest {
    private val chapters = listOf(
        readChapter(
            chapterNumber = 1,
            verseCount = 3,
        ),
        readChapter(
            chapterNumber = 2,
            verseCount = 4,
        ),
    )

    @Test
    fun `GIVEN the study card below WHEN getting start indices THEN a chapter spans its verses plus 3 items`() {
        // Given
        val isChapterStudyBeside = false

        // When
        val indices = getChapterStartIndices(
            chapters = chapters,
            leadingItemCount = 2,
            isChapterStudyBeside = isChapterStudyBeside,
        )

        // Then
        assertEquals(
            expected = listOf(2, 2 + 3 + 3, 2 + 3 + 3 + 4 + 3),
            actual = indices,
        )
    }

    @Test
    fun `GIVEN the study card beside WHEN getting start indices THEN a chapter spans its verses plus 2 items`() {
        // Given
        val isChapterStudyBeside = true

        // When
        val indices = getChapterStartIndices(
            chapters = chapters,
            leadingItemCount = 2,
            isChapterStudyBeside = isChapterStudyBeside,
        )

        // Then
        assertEquals(
            expected = listOf(2, 2 + 3 + 2, 2 + 3 + 2 + 4 + 2),
            actual = indices,
        )
    }
}
