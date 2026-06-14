package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.data.datasource.FavoritesRemoteDataSource
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.first

internal class PushPendingFavoritesUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val favoritesRemoteDataSource: FavoritesRemoteDataSource,
    private val bookDao: BookDao,
    private val bookFavoriteMapper: BookFavoriteMapper,
) : PushPendingFavorites {
    override suspend fun invoke() {
        val userId = observeAuthenticatedUserId().first() ?: return
        val pending = bookDao.getPendingFavoriteSyncBooks()
        if (pending.isEmpty()) return

        favoritesRemoteDataSource.upsertFavorites(pending.map { bookFavoriteMapper.toDto(userId, it) })
        pending.forEach { entity ->
            entity.favoriteUpdatedAt?.let { syncedUpdatedAt ->
                bookDao.markFavoriteSynced(entity.id, syncedUpdatedAt)
            }
        }
    }
}
