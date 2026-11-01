package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

private const val SCHEMA = "public"
const val USER_ID_COLUMN = "user_id"

// Why: removeChannel suspends, so a cancelled collector only reaches it under NonCancellable; otherwise the channel leaks.
fun <T : Any> Realtime.observeUserRows(
    table: String,
    userId: String,
    userIdColumn: String,
    toChange: (PostgresAction) -> T?,
): Flow<T> = flow {
    val channel = channel("${table}_$userId")
    val changes = channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA) {
        this.table = table
        filter(userIdColumn, FilterOperator.EQ, userId)
    }
    channel.subscribe()
    try {
        changes.collect { action -> toChange(action)?.let { emit(it) } }
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
) { action -> action.decodeUpsert<T>() }

inline fun <reified T : Any> PostgresAction.decodeUpsert(): T? = when (this) {
    is PostgresAction.Insert -> decodeRecord<T>()
    is PostgresAction.Update -> decodeRecord<T>()
    else -> null
}
