package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetAdjacentListeningChapterUseCaseTest {
    private val chapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private lateinit var useCase: GetAdjacentListeningChapterUseCase
    private lateinit var canonOrderRequests: MutableList<Boolean>

    @Test
    fun `GIVEN a next chapter in the reading order WHEN asking for the next THEN returns it`() = runTest {
        // Given
        prepareScenario()

        // When
        val next = useCase(
            chapter = chapter,
            direction = ChapterDirectionModel.NEXT,
            shouldForceCanonOrder = true,
        )

        // Then
        assertEquals(ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3), next)
        assertEquals(listOf(true), canonOrderRequests)
    }

    @Test
    fun `GIVEN the first chapter of the order WHEN asking for the previous THEN has none`() = runTest {
        // Given
        prepareScenario()

        // When
        val previous = useCase(
            chapter = chapter,
            direction = ChapterDirectionModel.PREVIOUS,
            shouldForceCanonOrder = false,
        )

        // Then
        assertNull(previous)
    }

    private fun prepareScenario() {
        canonOrderRequests = mutableListOf()
        useCase = GetAdjacentListeningChapterUseCase(
            getNextChapter = { bookId, chapterNumber, shouldForceCanonOrder ->
                canonOrderRequests += shouldForceCanonOrder
                ReadNavigationSuggestionModel(
                    bookId = bookId,
                    chapterNumber = chapterNumber + 1,
                )
            },
            getPreviousChapter = { _, _, _ -> null },
        )
    }
}
