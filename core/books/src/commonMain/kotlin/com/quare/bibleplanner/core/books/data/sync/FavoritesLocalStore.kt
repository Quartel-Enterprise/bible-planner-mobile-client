package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

/**
 * Adapts the favorites state on the `books` table to the generic sync engine. Favorite state lives as
 * columns on each book row ([BookEntity.isFavorite], [BookEntity.favoriteUpdatedAt],
 * [BookEntity.isFavoritePendingSync]).
 */
internal class FavoritesLocalStore(
    private val bookDao: BookDao,
    private val bookFavoriteMapper: BookFavoriteMapper,
) : SyncLocalStore<BookEntity, BookFavoriteDto> {
    override fun pendingFlow(): Flow<List<BookEntity>> = bookDao.getPendingFavoriteSyncBooksFlow()

    override suspend fun getPending(): List<BookEntity> = bookDao.getPendingFavoriteSyncBooks()

    override suspend fun markSynced(entity: BookEntity) {
        entity.favoriteUpdatedAt?.let { syncedUpdatedAt ->
            bookDao.markFavoriteSynced(
                bookId = entity.id,
                syncedUpdatedAt = syncedUpdatedAt,
            )
        }
    }

    override suspend fun applyRemote(dto: BookFavoriteDto) {
        bookDao.applyRemoteFavorite(
            bookId = dto.bookId,
            isFavorite = dto.isFavorite,
            remoteUpdatedAt = bookFavoriteMapper.toEpochMillis(dto.updatedAt),
        )
    }

    override fun toDto(
        userId: String,
        entity: BookEntity,
    ): BookFavoriteDto = bookFavoriteMapper.toDto(
        userId = userId,
        entity = entity,
    )

    /** Marks pre-sync favorites pending on first launch so they reach the backend. */
    override suspend fun seed(now: Long) {
        bookDao.markLegacyFavoritesPending(now)
    }

    override suspend fun clearLocal() {
        bookDao.resetAllFavorites()
    }
}
