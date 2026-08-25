package com.quare.bibleplanner.core.provider.room.invalidation

import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomTableInvalidationObserver(
    private val database: AppDatabase,
) : TableInvalidationObserver {
    override fun invoke(vararg tables: String): Flow<Unit> = database.invalidationTracker
        .createFlow(tables = tables)
        .map { }
}
