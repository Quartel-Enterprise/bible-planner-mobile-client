package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.ChapterReadDto
import com.quare.bibleplanner.core.books.data.mapper.ChapterReadMapper
import com.quare.bibleplanner.core.books.fake.ThrowingChapterDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChapterReadLocalStoreTest {
    private val pendingChapter = ChapterEntity(
        id = 7,
        number = 3,
        bookId = "GEN",
        isRead = true,
        readUpdatedAt = 1_000L,
        isReadPendingSync = true,
    )

    private val remoteRead = ChapterReadDto(
        userId = "user-1",
        bookId = "GEN",
        chapterNumber = 3,
        isRead = true,
        updatedAt = "1970-01-01T00:00:02Z",
    )

    private lateinit var store: ChapterReadLocalStore
    private lateinit var chapterDao: RecordingChapterDao
    private lateinit var verseDao: CascadeRecordingVerseDao

    @Test
    fun `GIVEN pending chapter reads WHEN reading them THEN returns the dao rows for both the flow and the snapshot`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val observed = store.observePending().first()
            val snapshot = store.getPending()

            // Then
            assertEquals(listOf(pendingChapter), observed)
            assertEquals(listOf(pendingChapter), snapshot)
        }

    @Test
    fun `GIVEN a pushed chapter WHEN marking it synced THEN clears it at the pushed timestamp`() = runTest {
        // Given
        prepareScenario()

        // When
        store.markSynced(pendingChapter)

        // Then
        assertEquals(listOf("markChapterReadSynced(GEN, 3, 1000)"), chapterDao.calls)
    }

    @Test
    fun `GIVEN a chapter without a read timestamp WHEN marking it synced THEN does nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        store.markSynced(pendingChapter.copy(readUpdatedAt = null))

        // Then
        assertTrue(chapterDao.calls.isEmpty())
    }

    @Test
    fun `GIVEN a newer remote read WHEN applying it THEN cascades the read state to the chapter verses`() = runTest {
        // Given
        prepareScenario(changedRows = 1)

        // When
        store.applyRemote(remoteRead)

        // Then
        assertEquals(listOf("applyRemoteChapterRead(GEN, 3, true, 2000)"), chapterDao.calls)
        assertEquals(listOf("cascadeChapterReadToVerses(GEN, 3, true)"), verseDao.calls)
    }

    @Test
    fun `GIVEN an older remote read WHEN applying it THEN leaves the verses untouched`() = runTest {
        // Given
        prepareScenario(changedRows = 0)

        // When
        store.applyRemote(remoteRead)

        // Then
        assertTrue(verseDao.calls.isEmpty())
    }

    @Test
    fun `GIVEN a pending chapter WHEN converting it THEN builds the remote row for the user`() = runTest {
        // Given
        prepareScenario()

        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = pendingChapter,
        )

        // Then
        assertEquals(
            ChapterReadDto(
                userId = "user-1",
                bookId = "GEN",
                chapterNumber = 3,
                isRead = true,
                updatedAt = "1970-01-01T00:00:01Z",
            ),
            dto,
        )
    }

    @Test
    fun `GIVEN a first launch WHEN seeding and clearing THEN delegates to the chapter dao`() = runTest {
        // Given
        prepareScenario()

        // When
        store.seed(now = 5L)
        store.clearLocal()

        // Then
        assertEquals(listOf("markLegacyChapterReadsPending(5)", "clearAllChapterReadSync"), chapterDao.calls)
    }

    private fun prepareScenario(changedRows: Int = 1) {
        chapterDao = RecordingChapterDao(
            pending = listOf(pendingChapter),
            changedRows = changedRows,
        )
        verseDao = CascadeRecordingVerseDao()
        store = ChapterReadLocalStore(
            chapterDao = chapterDao,
            verseDao = verseDao,
            chapterReadMapper = ChapterReadMapper(),
        )
    }
}

private class RecordingChapterDao(
    private val pending: List<ChapterEntity>,
    private val changedRows: Int,
) : ThrowingChapterDao() {
    val calls = mutableListOf<String>()

    override fun getPendingReadSyncChaptersFlow(): Flow<List<ChapterEntity>> = flowOf(pending)

    override suspend fun getPendingReadSyncChapters(): List<ChapterEntity> = pending

    override suspend fun markChapterReadSynced(
        bookId: String,
        chapterNumber: Int,
        syncedUpdatedAt: Long,
    ) {
        calls += "markChapterReadSynced($bookId, $chapterNumber, $syncedUpdatedAt)"
    }

    override suspend fun applyRemoteChapterRead(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    ): Int {
        calls += "applyRemoteChapterRead($bookId, $chapterNumber, $isRead, $remoteUpdatedAt)"
        return changedRows
    }

    override suspend fun markLegacyChapterReadsPending(now: Long) {
        calls += "markLegacyChapterReadsPending($now)"
    }

    override suspend fun clearAllChapterReadSync() {
        calls += "clearAllChapterReadSync"
    }
}

private class CascadeRecordingVerseDao : ThrowingVerseDao() {
    val calls = mutableListOf<String>()

    override suspend fun cascadeChapterReadToVerses(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
    ) {
        calls += "cascadeChapterReadToVerses($bookId, $chapterNumber, $isRead)"
    }
}
