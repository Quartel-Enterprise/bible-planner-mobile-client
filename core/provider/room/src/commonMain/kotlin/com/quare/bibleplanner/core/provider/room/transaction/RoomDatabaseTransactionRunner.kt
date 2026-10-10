package com.quare.bibleplanner.core.provider.room.transaction

import androidx.room3.withWriteTransaction
import com.quare.bibleplanner.core.provider.room.db.AppDatabase

internal class RoomDatabaseTransactionRunner(
    private val database: AppDatabase,
) : DatabaseTransactionRunner {
    override suspend fun invoke(block: suspend () -> Unit) {
        database.withWriteTransaction { block() }
    }
}
