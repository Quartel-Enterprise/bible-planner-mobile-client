package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BibleContentDaosTest {
    private lateinit var database: AppDatabase

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a book with chapters and verses WHEN reading it THEN nests its chapters and verses`() = runTest {
        // Given
        seedGenesisChapterOne()

        // When
        val book = database.bookDao().getBookWithChaptersById(BOOK_ID)

        // Then
        assertEquals(
            expected = BookEntity(
                id = BOOK_ID,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            ),
            actual = book?.book,
        )
        assertEquals(
            expected = listOf(1, 2),
            actual = book
                ?.chapters
                ?.single()
                ?.verses
                ?.map { it.number },
        )
    }

    @Test
    fun `GIVEN a downloaded version WHEN reading it THEN keeps its download status`() = runTest {
        // Given
        val version = BibleVersionEntity(
            id = VERSION_ID,
            status = DownloadStatus.PAUSED,
            totalChapters = 1189,
            contentVersion = "1.0.0",
        )
        database.bibleVersionDao().insertVersion(version)

        // When
        val stored = database.bibleVersionDao().getVersionById(VERSION_ID)

        // Then
        assertEquals(
            expected = version,
            actual = stored,
        )
    }

    @Test
    fun `GIVEN verse texts WHEN reading the verses THEN attaches each text and counts the downloaded chapters`() =
        runTest {
            // Given
            val verseIds = seedGenesisChapterOne()
            database.bibleVersionDao().insertVersion(
                BibleVersionEntity(
                    id = VERSION_ID,
                    status = DownloadStatus.DONE,
                ),
            )
            database.verseDao().upsertVerseTexts(
                listOf(
                    VerseTextEntity(
                        verseId = verseIds.first(),
                        bibleVersionId = VERSION_ID,
                        text = "In the beginning",
                    ),
                ),
            )

            // When
            val verses = database.verseDao().getVersesWithTextsByChapterId(CHAPTER_ID)

            // Then
            assertEquals(
                expected = listOf(listOf("In the beginning"), emptyList()),
                actual = verses.map { verse -> verse.texts.map { it.text } },
            )
            assertEquals(
                expected = listOf(
                    VersionChapterCount(
                        bibleVersionId = VERSION_ID,
                        downloadedChapters = 1,
                    ),
                ),
                actual = database.verseDao().getDownloadedChaptersPerVersion(),
            )
        }

    @Test
    fun `GIVEN a verse range read WHEN listing the pending reads THEN addresses each verse by its chapter`() = runTest {
        // Given
        seedGenesisChapterOne()

        // When
        database.verseDao().updateVerseReadStatusRange(
            chapterId = CHAPTER_ID,
            startVerse = 1,
            endVerse = 2,
            isRead = true,
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = listOf(1, 2).map { verseNumber ->
                PendingVerseRead(
                    bookId = BOOK_ID,
                    chapterNumber = 1,
                    verseNumber = verseNumber,
                    isRead = true,
                    readUpdatedAt = 10L,
                )
            },
            actual = database.verseDao().getPendingReadSyncVerses(),
        )
    }

    private suspend fun seedGenesisChapterOne(): List<Long> {
        database.bookDao().insertBook(
            BookEntity(
                id = BOOK_ID,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            ),
        )
        database.chapterDao().insertChapter(
            ChapterEntity(
                id = CHAPTER_ID,
                number = 1,
                bookId = BOOK_ID,
            ),
        )
        return database.verseDao().upsertVerses(
            listOf(1, 2).map { number ->
                VerseEntity(
                    id = 0,
                    number = number,
                    chapterId = CHAPTER_ID,
                )
            },
        )
    }

    private companion object {
        const val BOOK_ID = "GEN"
        const val CHAPTER_ID = 1L
        const val VERSION_ID = "ACF"
    }
}
