package com.quare.bibleplanner.core.books.domain.usecase

import bibleplanner.core.books.generated.resources.Res
import bibleplanner.core.books.generated.resources.book_jhn
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.books.fake.FakeBibleRepository
import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetVersesShareContentUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: GetVersesShareContentUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.JHN,
            versesPerChapter = listOf(0, 0, 4),
        )
        database.verses
            .filter { it.number <= 3 }
            .forEach { verse ->
                database.verseTexts += VerseTextEntity(
                    verseId = verse.id,
                    bibleVersionId = "web",
                    text = " Text ${verse.number} ",
                )
                database.verseTexts += VerseTextEntity(
                    verseId = verse.id,
                    bibleVersionId = "kjv",
                    text = "Other ${verse.number}",
                )
            }
        useCase = GetVersesShareContentUseCase(
            getChapterId = GetChapterIdUseCase(database.chapterDao),
            verseDao = database.verseDao,
            getSelectedVersionIdFlow = GetSelectedVersionIdFlowUseCase(
                FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "web",
                ),
            ),
        )
    }

    @Test
    fun `GIVEN several verses WHEN sharing THEN numbers each trimmed verse in order under a compact reference`() =
        runTest {
            // When
            val result = useCase(
                bookId = BookId.JHN,
                chapterNumber = 3,
                verseNumbers = listOf(3, 1, 2),
            )

            // Then
            assertEquals(
                VersesShareContentModel(
                    text = "[1] Text 1\n[2] Text 2\n[3] Text 3",
                    reference = "${getString(Res.string.book_jhn)} 3:1-3",
                    versionAbbreviation = "WEB",
                ),
                result,
            )
        }

    @Test
    fun `GIVEN a single verse WHEN sharing THEN omits the verse number from the text`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.JHN,
            chapterNumber = 3,
            verseNumbers = listOf(2),
        )

        // Then
        assertEquals("Text 2", result?.text)
        assertEquals("${getString(Res.string.book_jhn)} 3:2", result?.reference)
    }

    @Test
    fun `GIVEN a verse without text in the selected version WHEN sharing THEN leaves it out of the reference`() =
        runTest {
            // When
            val result = useCase(
                bookId = BookId.JHN,
                chapterNumber = 3,
                verseNumbers = listOf(3, 4),
            )

            // Then
            assertEquals("${getString(Res.string.book_jhn)} 3:3", result?.reference)
        }

    @Test
    fun `GIVEN no verse text in the selected version WHEN sharing THEN returns null`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.JHN,
            chapterNumber = 3,
            verseNumbers = listOf(4),
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `GIVEN a chapter missing from the database WHEN sharing THEN returns null`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.GEN,
            chapterNumber = 1,
            verseNumbers = listOf(1),
        )

        // Then
        assertNull(result)
    }
}
