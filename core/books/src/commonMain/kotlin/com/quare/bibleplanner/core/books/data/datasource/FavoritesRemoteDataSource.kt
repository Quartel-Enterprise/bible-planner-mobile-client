package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface FavoritesRemoteDataSource {
    suspend fun upsertFavorites(favorites: List<BookFavoriteDto>)

    suspend fun fetchFavorites(userId: String): List<BookFavoriteDto>

    fun getRealtimeStatusFlow(): StateFlow<Realtime.Status>

    /**
     * Emits remote favorite rows (inserts and updates) for [userId] in real time. Deletes are ignored:
     * unfavoriting is modeled as an upsert with is_favorite = false, so a delete carries no state to apply.
     */
    fun observeRemoteFavorites(userId: String): Flow<BookFavoriteDto>
}
