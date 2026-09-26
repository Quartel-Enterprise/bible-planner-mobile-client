package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.books.fake.ThrowingBookDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FavoritesLocalStoreTest {
    private val pendingBook = BookEntity(
        id = "PSA",
        isRead = false,
        isFavorite = true,
        favoriteUpdatedAt = 1_000L,
        isFavoritePendingSync = true,
    )

    private lateinit var bookDao: RecordingBookDao
    private lateinit var store: FavoritesLocalStore

    @BeforeTest
    fun setUp() {
        bookDao = RecordingBookDao(listOf(pendingBook))
        store = FavoritesLocalStore(
            bookDao = bookDao,
            bookFavoriteMapper = BookFavoriteMapper(),
        )
    }

    @Test
    fun `GIVEN pending favorites WHEN reading them THEN returns the dao rows for both the flow and the snapshot`() =
        runTest {
            // When
            val observed = store.observePending().first()
            val snapshot = store.getPending()

            // Then
            assertEquals(listOf(pendingBook), observed)
            assertEquals(listOf(pendingBook), snapshot)
        }

    @Test
    fun `GIVEN a pushed favorite WHEN marking it synced THEN clears it at the pushed timestamp`() = runTest {
        // When
        store.markSynced(pendingBook)

        // Then
        assertEquals(listOf("markFavoriteSynced(PSA, 1000)"), bookDao.calls)
    }

    @Test
    fun `GIVEN a favorite without timestamp WHEN marking it synced THEN does nothing`() = runTest {
        // When
        store.markSynced(pendingBook.copy(favoriteUpdatedAt = null))

        // Then
        assertTrue(bookDao.calls.isEmpty())
    }

    @Test
    fun `GIVEN a remote favorite WHEN applying it THEN stores it with its epoch timestamp`() = runTest {
        // When
        store.applyRemote(
            BookFavoriteDto(
                userId = "user-1",
                bookId = "PSA",
                isFavorite = false,
                updatedAt = "1970-01-01T00:00:02Z",
            ),
        )

        // Then
        assertEquals(listOf("applyRemoteFavorite(PSA, false, 2000)"), bookDao.calls)
    }

    @Test
    fun `GIVEN a pending favorite WHEN converting it THEN builds the remote row for the user`() {
        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = pendingBook,
        )

        // Then
        assertEquals(
            BookFavoriteDto(
                userId = "user-1",
                bookId = "PSA",
                isFavorite = true,
                updatedAt = "1970-01-01T00:00:01Z",
            ),
            dto,
        )
    }

    @Test
    fun `GIVEN a first launch WHEN seeding and clearing THEN delegates to the book dao`() = runTest {
        // When
        store.seed(now = 5L)
        store.clearLocal()

        // Then
        assertEquals(listOf("markLegacyFavoritesPending(5)", "resetAllFavorites"), bookDao.calls)
    }
}

private class RecordingBookDao(
    private val pending: List<BookEntity>,
) : ThrowingBookDao() {
    val calls = mutableListOf<String>()

    override fun getPendingFavoriteSyncBooksFlow(): Flow<List<BookEntity>> = flowOf(pending)

    override suspend fun getPendingFavoriteSyncBooks(): List<BookEntity> = pending

    override suspend fun markFavoriteSynced(
        bookId: String,
        syncedUpdatedAt: Long,
    ) {
        calls += "markFavoriteSynced($bookId, $syncedUpdatedAt)"
    }

    override suspend fun applyRemoteFavorite(
        bookId: String,
        isFavorite: Boolean,
        remoteUpdatedAt: Long,
    ) {
        calls += "applyRemoteFavorite($bookId, $isFavorite, $remoteUpdatedAt)"
    }

    override suspend fun markLegacyFavoritesPending(now: Long) {
        calls += "markLegacyFavoritesPending($now)"
    }

    override suspend fun resetAllFavorites() {
        calls += "resetAllFavorites"
    }
}
