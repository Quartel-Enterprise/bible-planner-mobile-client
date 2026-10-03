package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: VerseDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.verseDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a verse already read WHEN marking its range read THEN leaves it untouched and not pending`() = runTest {
        // Given
        val alreadyRead = verse(
            number = 1,
            isRead = true,
            readUpdatedAt = 5L,
            isReadPendingSync = false,
        )
        val unread = verse(
            number = 2,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
        val outsideRange = verse(
            number = 3,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
        seedChapter(listOf(alreadyRead, unread, outsideRange))

        // When
        dao.updateVerseReadStatusRange(
            chapterId = CHAPTER_ID,
            startVerse = 1,
            endVerse = 2,
            isRead = true,
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = listOf(
                alreadyRead,
                unread.copy(
                    isRead = true,
                    readUpdatedAt = 10L,
                    isReadPendingSync = true,
                ),
                outsideRange,
            ),
            actual = dao.getVersesByChapterId(CHAPTER_ID),
        )
    }

    @Test
    fun `GIVEN unread verses WHEN marking the range unread THEN leaves them untouched and not pending`() = runTest {
        // Given
        val verses = listOf(1, 2).map { number ->
            verse(
                number = number,
                isRead = false,
                readUpdatedAt = 5L,
                isReadPendingSync = false,
            )
        }
        seedChapter(verses)

        // When
        dao.updateVerseReadStatusRange(
            chapterId = CHAPTER_ID,
            startVerse = 1,
            endVerse = 2,
            isRead = false,
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = verses,
            actual = dao.getVersesByChapterId(CHAPTER_ID),
        )
        assertEquals(
            expected = emptyList(),
            actual = dao.getPendingReadSyncVerses(),
        )
    }

    @Test
    fun `GIVEN a verse with a pending range edit WHEN cascading a chapter read THEN skips that verse`() = runTest {
        // Given
        val pending = verse(
            number = 1,
            isRead = false,
            readUpdatedAt = 5L,
            isReadPendingSync = true,
        )
        val synced = verse(
            number = 2,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
        seedChapter(listOf(pending, synced))

        // When
        dao.cascadeChapterReadToVerses(
            bookId = BOOK_ID,
            chapterNumber = CHAPTER_NUMBER,
            isRead = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                pending,
                synced.copy(isRead = true),
            ),
            actual = dao.getVersesByChapterId(CHAPTER_ID),
        )
    }

    @Test
    fun `GIVEN synced and never synced verses WHEN resetting reads THEN restamps and flags only the synced ones`() =
        runTest {
            // Given
            val synced = verse(
                number = 1,
                isRead = true,
                readUpdatedAt = 5L,
                isReadPendingSync = false,
            )
            val neverSynced = verse(
                number = 2,
                isRead = true,
                readUpdatedAt = null,
                isReadPendingSync = false,
            )
            seedChapter(listOf(synced, neverSynced))

            // When
            dao.resetAllVerseReadsForSync(now = 20L)

            // Then
            assertEquals(
                expected = listOf(
                    synced.copy(
                        isRead = false,
                        readUpdatedAt = 20L,
                        isReadPendingSync = true,
                    ),
                    neverSynced.copy(isRead = false),
                ),
                actual = dao.getVersesByChapterId(CHAPTER_ID),
            )
        }

    private suspend fun seedChapter(verses: List<VerseEntity>) {
        database.bookDao().insertBook(
            BookEntity(
                id = BOOK_ID,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
                isRead = false,
                isFavorite = false,
            ),
        )
        database.chapterDao().insertChapter(
            ChapterEntity(
                id = CHAPTER_ID,
                number = CHAPTER_NUMBER,
                bookId = BOOK_ID,
                isRead = false,
                readUpdatedAt = null,
                isReadPendingSync = false,
            ),
        )
        dao.upsertVerses(verses)
    }

    private fun verse(
        number: Int,
        isRead: Boolean,
        readUpdatedAt: Long?,
        isReadPendingSync: Boolean,
    ): VerseEntity = VerseEntity(
        id = number.toLong(),
        number = number,
        chapterId = CHAPTER_ID,
        isRead = isRead,
        readUpdatedAt = readUpdatedAt,
        isReadPendingSync = isReadPendingSync,
    )

    private companion object {
        const val BOOK_ID = "GEN"
        const val CHAPTER_ID = 1L
        const val CHAPTER_NUMBER = 1
    }
}
