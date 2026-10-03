package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChapterDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ChapterDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.chapterDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a chapter already read WHEN marking it read THEN leaves it untouched and not pending`() = runTest {
        // Given
        val alreadyRead = chapter(
            number = 1,
            isRead = true,
            readUpdatedAt = 5L,
            isReadPendingSync = false,
        )
        seedBook(listOf(alreadyRead))

        // When
        dao.updateChapterReadStatus(
            chapterId = alreadyRead.id,
            isRead = true,
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = listOf(alreadyRead),
            actual = dao.getChaptersByBookId(BOOK_ID),
        )
        assertEquals(
            expected = emptyList(),
            actual = dao.getPendingReadSyncChapters(),
        )
    }

    @Test
    fun `GIVEN an unread chapter WHEN marking it read THEN flags it pending with the new timestamp`() = runTest {
        // Given
        val unread = chapter(
            number = 1,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
        seedBook(listOf(unread))

        // When
        dao.updateChapterReadStatus(
            chapterId = unread.id,
            isRead = true,
            updatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = listOf(
                unread.copy(
                    isRead = true,
                    readUpdatedAt = 10L,
                    isReadPendingSync = true,
                ),
            ),
            actual = dao.getChaptersByBookId(BOOK_ID),
        )
    }

    @Test
    fun `GIVEN read and unread chapters WHEN marking the book read THEN flags only the unread ones pending`() =
        runTest {
            // Given
            val alreadyRead = chapter(
                number = 1,
                isRead = true,
                readUpdatedAt = 5L,
                isReadPendingSync = false,
            )
            val unread = chapter(
                number = 2,
                isRead = false,
                readUpdatedAt = null,
                isReadPendingSync = false,
            )
            seedBook(listOf(alreadyRead, unread))

            // When
            dao.updateChaptersReadStatusByBook(
                bookId = BOOK_ID,
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
                ),
                actual = dao.getChaptersByBookId(BOOK_ID),
            )
        }

    @Test
    fun `GIVEN a pending chapter WHEN applying a newer remote read THEN skips it and reports no change`() = runTest {
        // Given
        val pending = chapter(
            number = 1,
            isRead = false,
            readUpdatedAt = 5L,
            isReadPendingSync = true,
        )
        seedBook(listOf(pending))

        // When
        val changed = applyRemoteRead(remoteUpdatedAt = 10L)

        // Then
        assertEquals(
            expected = 0,
            actual = changed,
        )
        assertEquals(
            expected = listOf(pending),
            actual = dao.getChaptersByBookId(BOOK_ID),
        )
    }

    @Test
    fun `GIVEN a chapter newer than the remote read WHEN applying it THEN skips it and reports no change`() = runTest {
        // Given
        val newer = chapter(
            number = 1,
            isRead = false,
            readUpdatedAt = 20L,
            isReadPendingSync = false,
        )
        seedBook(listOf(newer))

        // When
        val changed = applyRemoteRead(remoteUpdatedAt = 10L)

        // Then
        assertEquals(
            expected = 0,
            actual = changed,
        )
        assertEquals(
            expected = listOf(newer),
            actual = dao.getChaptersByBookId(BOOK_ID),
        )
    }

    @Test
    fun `GIVEN a chapter as recent as the remote read WHEN applying it THEN skips it and reports no change`() =
        runTest {
            // Given
            val sameAge = chapter(
                number = 1,
                isRead = false,
                readUpdatedAt = 10L,
                isReadPendingSync = false,
            )
            seedBook(listOf(sameAge))

            // When
            val changed = applyRemoteRead(remoteUpdatedAt = 10L)

            // Then
            assertEquals(
                expected = 0,
                actual = changed,
            )
            assertEquals(
                expected = listOf(sameAge),
                actual = dao.getChaptersByBookId(BOOK_ID),
            )
        }

    @Test
    fun `GIVEN an older chapter not pending WHEN applying a remote read THEN applies it without flagging it pending`() =
        runTest {
            // Given
            val older = chapter(
                number = 1,
                isRead = false,
                readUpdatedAt = 5L,
                isReadPendingSync = false,
            )
            seedBook(listOf(older))

            // When
            val changed = applyRemoteRead(remoteUpdatedAt = 10L)

            // Then
            assertEquals(
                expected = 1,
                actual = changed,
            )
            assertEquals(
                expected = listOf(
                    older.copy(
                        isRead = true,
                        readUpdatedAt = 10L,
                    ),
                ),
                actual = dao.getChaptersByBookId(BOOK_ID),
            )
        }

    @Test
    fun `GIVEN a never synced chapter WHEN applying a remote read THEN applies it`() = runTest {
        // Given
        val neverSynced = chapter(
            number = 1,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
        seedBook(listOf(neverSynced))

        // When
        val changed = applyRemoteRead(remoteUpdatedAt = 10L)

        // Then
        assertEquals(
            expected = 1,
            actual = changed,
        )
        assertEquals(
            expected = listOf(
                neverSynced.copy(
                    isRead = true,
                    readUpdatedAt = 10L,
                ),
            ),
            actual = dao.getChaptersByBookId(BOOK_ID),
        )
    }

    private suspend fun applyRemoteRead(remoteUpdatedAt: Long): Int = dao.applyRemoteChapterRead(
        bookId = BOOK_ID,
        chapterNumber = 1,
        isRead = true,
        remoteUpdatedAt = remoteUpdatedAt,
    )

    private suspend fun seedBook(chapters: List<ChapterEntity>) {
        database.bookDao().insertBook(
            BookEntity(
                id = BOOK_ID,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
                isRead = false,
                isFavorite = false,
            ),
        )
        dao.insertChapters(chapters)
    }

    private fun chapter(
        number: Int,
        isRead: Boolean,
        readUpdatedAt: Long?,
        isReadPendingSync: Boolean,
    ): ChapterEntity = ChapterEntity(
        id = number.toLong(),
        number = number,
        bookId = BOOK_ID,
        isRead = isRead,
        readUpdatedAt = readUpdatedAt,
        isReadPendingSync = isReadPendingSync,
    )

    private companion object {
        const val BOOK_ID = "GEN"
    }
}
