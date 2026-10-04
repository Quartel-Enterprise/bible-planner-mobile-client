package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetListeningChapterTitleUseCaseTest {
    private val useCase = GetListeningChapterTitleUseCase()

    @Test
    fun `GIVEN a chapter WHEN getting its title THEN joins the book name and the chapter number`() = runTest {
        // Given
        val chapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3)

        // When
        val title = useCase(chapter)

        // Then
        assertEquals("${getString(BookId.GEN.toBookNameResource())} 3", title)
    }
}
