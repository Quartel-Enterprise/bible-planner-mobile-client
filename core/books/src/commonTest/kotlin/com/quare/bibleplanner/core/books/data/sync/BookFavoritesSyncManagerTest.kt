package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.datasource.FavoritesRemoteDataSource
import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class BookFavoritesSyncManagerTest {
    private lateinit var dao: FakeBookDao
    private lateinit var remote: FakeFavoritesRemoteDataSource

    @Test
    fun `GIVEN a remote favorite change WHEN it arrives THEN it is applied to the dao`() = runTest {
        // Given
        prepareScenario(online = false)

        // When
        remote.remoteFavorites.emit(
            BookFavoriteDto(userId = "user-1", bookId = "GEN", isFavorite = true, updatedAt = "2026-06-12T08:00:00Z"),
        )
        runCurrent()

        // Then
        assertEquals(1, dao.applyRemoteFavoriteCalls.size)
        val call = dao.applyRemoteFavoriteCalls.first()
        assertEquals("GEN", call.bookId)
        assertTrue(call.isFavorite)
    }

    @Test
    fun `GIVEN pending favorites and online WHEN syncing THEN pushes them and marks them synced`() = runTest {
        // Given
        prepareScenario(online = true)

        // When
        dao.pendingFlow.value = listOf(pendingBook(id = "GEN", favoriteUpdatedAt = 100L))
        runCurrent()

        // Then
        assertEquals(1, remote.upsertCalls.size)
        assertEquals(
            "GEN",
            remote.upsertCalls
                .first()
                .single()
                .bookId,
        )
        assertEquals(listOf("GEN" to 100L), dao.markFavoriteSyncedCalls)
    }

    @Test
    fun `GIVEN pending favorites and offline WHEN syncing THEN does not push them`() = runTest {
        // Given
        prepareScenario(online = false)

        // When
        dao.pendingFlow.value = listOf(pendingBook(id = "GEN", favoriteUpdatedAt = 100L))
        runCurrent()

        // Then
        assertTrue(remote.upsertCalls.isEmpty())
    }

    private fun pendingBook(
        id: String,
        favoriteUpdatedAt: Long,
    ) = BookEntity(
        id = id,
        isFavorite = true,
        favoriteUpdatedAt = favoriteUpdatedAt,
        isFavoritePendingSync = true,
    )

    private fun TestScope.prepareScenario(online: Boolean) {
        dao = FakeBookDao()
        remote = FakeFavoritesRemoteDataSource()
        val manager = BookFavoritesSyncManager(
            observeAuthenticatedUserId = { flowOf("user-1") },
            favoritesRemoteDataSource = remote,
            bookDao = dao,
            bookFavoriteMapper = BookFavoriteMapper(),
            currentTimestampProvider = { 0L },
            networkConnectivityObserver = NetworkConnectivityObserver { flowOf(online) },
        )
        backgroundScope.launch { manager() }
        runCurrent()
    }
}

private class FakeFavoritesRemoteDataSource : FavoritesRemoteDataSource {
    val upsertCalls = mutableListOf<List<BookFavoriteDto>>()
    val remoteFavorites = MutableSharedFlow<BookFavoriteDto>()
    private val status = MutableStateFlow(Realtime.Status.DISCONNECTED)

    override suspend fun upsertFavorites(favorites: List<BookFavoriteDto>) {
        upsertCalls += favorites
    }

    override suspend fun fetchFavorites(userId: String): List<BookFavoriteDto> = emptyList()

    override fun getRealtimeStatusFlow(): StateFlow<Realtime.Status> = status

    override fun observeRemoteFavorites(userId: String): Flow<BookFavoriteDto> = remoteFavorites
}

private class FakeBookDao : BookDao {
    val pendingFlow = MutableStateFlow<List<BookEntity>>(emptyList())
    val applyRemoteFavoriteCalls = mutableListOf<ApplyRemoteFavorite>()
    val markFavoriteSyncedCalls = mutableListOf<Pair<String, Long>>()

    data class ApplyRemoteFavorite(
        val bookId: String,
        val isFavorite: Boolean,
        val remoteUpdatedAt: Long,
    )

    override fun getPendingFavoriteSyncBooksFlow(): Flow<List<BookEntity>> = pendingFlow

    override suspend fun markLegacyFavoritesPending(now: Long) = Unit

    override suspend fun markFavoriteSynced(
        bookId: String,
        syncedUpdatedAt: Long,
    ) {
        markFavoriteSyncedCalls += bookId to syncedUpdatedAt
    }

    override suspend fun applyRemoteFavorite(
        bookId: String,
        isFavorite: Boolean,
        remoteUpdatedAt: Long,
    ) {
        applyRemoteFavoriteCalls += ApplyRemoteFavorite(bookId, isFavorite, remoteUpdatedAt)
    }

    override fun getAllBooksFlow(): Flow<List<BookEntity>> = error("unused")

    override suspend fun getBookById(bookId: String): BookEntity? = error("unused")

    override suspend fun getAllBooksWithChapters(): List<BookWithChapters> = error("unused")

    override fun getAllBooksWithChaptersFlow(): Flow<List<BookWithChapters>> = error("unused")

    override fun getBookWithChaptersByIdFlow(bookId: String): Flow<BookWithChapters?> = error("unused")

    override suspend fun getBookWithChaptersById(bookId: String): BookWithChapters? = error("unused")

    override suspend fun insertBook(book: BookEntity) = error("unused")

    override suspend fun insertBooks(books: List<BookEntity>) = error("unused")

    override suspend fun updateBook(book: BookEntity) = error("unused")

    override suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    ) = error("unused")

    override suspend fun updateBookFavoriteStatus(
        bookId: String,
        isFavorite: Boolean,
        updatedAt: Long,
    ) = error("unused")

    override suspend fun getPendingFavoriteSyncBooks(): List<BookEntity> = error("unused")

    override suspend fun deleteBook(bookId: String) = error("unused")

    override suspend fun deleteAllBooks() = error("unused")

    override suspend fun resetAllBooksProgress() = error("unused")

    override suspend fun resetAllFavorites() = error("unused")
}
