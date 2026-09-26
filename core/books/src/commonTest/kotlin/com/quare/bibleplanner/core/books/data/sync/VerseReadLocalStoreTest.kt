package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.VerseReadDto
import com.quare.bibleplanner.core.books.data.mapper.VerseReadMapper
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseReadLocalStoreTest {
    private val pendingVerse = PendingVerseRead(
        bookId = "JHN",
        chapterNumber = 3,
        verseNumber = 16,
        isRead = true,
        readUpdatedAt = 1_000L,
    )

    private lateinit var verseDao: PendingVerseReadDao
    private lateinit var store: VerseReadLocalStore

    @BeforeTest
    fun setUp() {
        verseDao = PendingVerseReadDao(listOf(pendingVerse))
        store = VerseReadLocalStore(
            verseDao = verseDao,
            verseReadMapper = VerseReadMapper(),
        )
    }

    @Test
    fun `GIVEN pending verse reads WHEN reading them THEN returns the dao rows for both the flow and the snapshot`() =
        runTest {
            // When
            val observed = store.observePending().first()
            val snapshot = store.getPending()

            // Then
            assertEquals(listOf(pendingVerse), observed)
            assertEquals(listOf(pendingVerse), snapshot)
        }

    @Test
    fun `GIVEN a pushed verse read WHEN marking it synced THEN clears it at the pushed timestamp`() = runTest {
        // When
        store.markSynced(pendingVerse)

        // Then
        assertEquals(listOf("markVerseReadSynced(JHN, 3, 16, 1000)"), verseDao.calls)
    }

    @Test
    fun `GIVEN a verse read without timestamp WHEN marking it synced THEN does nothing`() = runTest {
        // When
        store.markSynced(pendingVerse.copy(readUpdatedAt = null))

        // Then
        assertTrue(verseDao.calls.isEmpty())
    }

    @Test
    fun `GIVEN a remote verse read WHEN applying it THEN stores it with its epoch timestamp`() = runTest {
        // When
        store.applyRemote(
            VerseReadDto(
                userId = "user-1",
                bookId = "JHN",
                chapterNumber = 3,
                verseNumber = 16,
                isRead = false,
                updatedAt = "1970-01-01T00:00:02Z",
            ),
        )

        // Then
        assertEquals(listOf("applyRemoteVerseRead(JHN, 3, 16, false, 2000)"), verseDao.calls)
    }

    @Test
    fun `GIVEN a pending verse read WHEN converting it THEN builds the remote row for the user`() {
        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = pendingVerse,
        )

        // Then
        assertEquals(
            VerseReadDto(
                userId = "user-1",
                bookId = "JHN",
                chapterNumber = 3,
                verseNumber = 16,
                isRead = true,
                updatedAt = "1970-01-01T00:00:01Z",
            ),
            dto,
        )
    }

    @Test
    fun `GIVEN a first launch WHEN seeding and clearing THEN delegates to the verse dao`() = runTest {
        // When
        store.seed(now = 5L)
        store.clearLocal()

        // Then
        assertEquals(listOf("markLegacyVerseReadsPending(5)", "clearAllVerseReadSync"), verseDao.calls)
    }
}

private class PendingVerseReadDao(
    private val pending: List<PendingVerseRead>,
) : ThrowingVerseDao() {
    val calls = mutableListOf<String>()

    override fun getPendingReadSyncVersesFlow(): Flow<List<PendingVerseRead>> = flowOf(pending)

    override suspend fun getPendingReadSyncVerses(): List<PendingVerseRead> = pending

    override suspend fun markVerseReadSynced(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    ) {
        calls += "markVerseReadSynced($bookId, $chapterNumber, $verseNumber, $syncedUpdatedAt)"
    }

    override suspend fun applyRemoteVerseRead(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    ) {
        calls += "applyRemoteVerseRead($bookId, $chapterNumber, $verseNumber, $isRead, $remoteUpdatedAt)"
    }

    override suspend fun markLegacyVerseReadsPending(now: Long) {
        calls += "markLegacyVerseReadsPending($now)"
    }

    override suspend fun clearAllVerseReadSync() {
        calls += "clearAllVerseReadSync"
    }
}
