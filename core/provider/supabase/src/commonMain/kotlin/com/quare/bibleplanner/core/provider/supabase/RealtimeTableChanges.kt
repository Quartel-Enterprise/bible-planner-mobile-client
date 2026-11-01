package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

private const val SCHEMA = "public"
const val USER_ID_COLUMN = "user_id"

/*
 * Why: removeChannel suspends, so a cancelled collector only reaches it under NonCancellable; otherwise the channel
 * leaks. Deletes come on their own unfiltered flow: Realtime strips an RLS table's deleted record to its primary key
 * (Postgres cannot check a policy on a gone row), so the user id filter would match none of them. A null [deleted]
 * subscribes to no deletes.
 */
fun <T : Any> Realtime.observeUserRows(
    table: String,
    userId: String,
    userIdColumn: String,
    upserted: (PostgresAction) -> T?,
    deleted: ((PostgresAction.Delete) -> T?)?,
): Flow<T> = flow {
    val channel = channel("${table}_$userId")
    val writes = channel
        .postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
            this.table = table
            filter(userIdColumn, FilterOperator.EQ, userId)
        }.filter { action -> action !is PostgresAction.Delete }
        .mapNotNull { action -> upserted(action) }
    val deletions = deleted?.let { toChange ->
        channel
            .postgresChangeFlow<PostgresAction.Delete>(schema = SCHEMA) {
                this.table = table
            }.mapNotNull { action -> toChange(action) }
    }
    channel.subscribe()
    try {
        listOfNotNull(writes, deletions).merge().collect { change -> emit(change) }
    } finally {
        withContext(NonCancellable) {
            removeChannel(channel)
        }
    }
}

inline fun <reified T : Any> Realtime.observeUserUpserts(
    table: String,
    userId: String,
): Flow<T> = observeUserRows(
    table = table,
    userId = userId,
    userIdColumn = USER_ID_COLUMN,
    upserted = { action -> action.decodeUpsert<T>() },
    deleted = null,
)

inline fun <reified T : Any> PostgresAction.decodeUpsert(): T? = when (this) {
    is PostgresAction.Insert -> decodeRecord<T>()
    is PostgresAction.Update -> decodeRecord<T>()
    else -> null
}
