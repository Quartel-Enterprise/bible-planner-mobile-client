package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class FavoritesRemoteDataSource(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
) {
    suspend fun upsertFavorites(favorites: List<BookFavoriteDto>) {
        supabaseClient.from(TABLE).upsert(favorites) {
            onConflict = "user_id,book_id"
        }
    }

    suspend fun fetchFavorites(userId: String): List<BookFavoriteDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList()

    fun getRealtimeStatusFlow(): StateFlow<Realtime.Status> = realtime.status

    /**
     * Emits remote favorite rows (inserts and updates) for [userId] in real time. Deletes are
     * ignored: unfavoriting is modeled as an upsert with is_favorite = false, so a delete carries
     * no state we need to apply. The channel is created and the change flow registered before
     * subscribing (required by supabase-kt) and removed when collection stops.
     */
    fun observeRemoteFavorites(userId: String): Flow<BookFavoriteDto> = flow {
        val channel = realtime.channel("book_favorites_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val dto = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<BookFavoriteDto>()
                    is PostgresAction.Update -> action.decodeRecord<BookFavoriteDto>()
                    else -> null
                }
                if (dto != null) emit(dto)
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    private companion object {
        const val TABLE = "book_favorites"
    }
}
