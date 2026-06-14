package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.data.datasource.FavoritesRemoteDataSource
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import io.github.jan.supabase.auth.Auth

internal class PushPendingFavoritesUseCase(
    private val auth: Auth,
    private val favoritesRemoteDataSource: FavoritesRemoteDataSource,
    private val bookDao: BookDao,
    private val bookFavoriteMapper: BookFavoriteMapper,
) : PushPendingFavorites {
    override suspend fun invoke() {
        val userId = auth.currentUserOrNull()?.id ?: return
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
