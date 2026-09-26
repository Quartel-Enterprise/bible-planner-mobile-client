package com.quare.bibleplanner.core.books.util

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.book_gen
import bibleplanner.core.books.generated.resources.book_oba
import bibleplanner.core.books.generated.resources.book_psa
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LocalizedReferenceLabelTest {
    @Test
    fun `GIVEN verse numbers WHEN building the reference label THEN appends the compacted verses to the chapter`() =
        runTest {
            // When
            val label = getVerseReferenceLabel(
                bookId = BookId.GEN,
                chapterNumber = 3,
                verseNumbers = listOf(7, 1, 2, 3),
            )

            // Then
            assertEquals("${getString(Res.string.book_gen)} 3:1-3, 7", label)
        }

    @Test
    fun `GIVEN no verse numbers WHEN building the reference label THEN names only the chapter`() = runTest {
        // When
        val label = getVerseReferenceLabel(
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = emptyList(),
        )

        // Then
        assertEquals("${getString(Res.string.book_gen)} 3", label)
    }

    @Test
    fun `GIVEN several passages WHEN building the reading label THEN joins each book with its chapter ranges`() =
        runTest {
            // Given
            val passages = listOf(
                passage(
                    bookId = BookId.PSA,
                    chapterRanges = "32, 122",
                ),
                passage(
                    bookId = BookId.OBA,
                    chapterRanges = null,
                ),
                passage(
                    bookId = BookId.GEN,
                    chapterRanges = "",
                ),
            )

            // When
            val label = passages.getReadingLabel()

            // Then
            assertEquals(
                "${getString(Res.string.book_psa)} 32, 122, ${getString(Res.string.book_oba)}, " +
                    getString(Res.string.book_gen),
                label,
            )
        }

    private fun passage(
        bookId: BookId,
        chapterRanges: String?,
    ): PassageModel = PassageModel(
        bookId = bookId,
        chapters = emptyList(),
        isRead = false,
        chapterRanges = chapterRanges,
    )
}
