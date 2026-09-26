package com.quare.bibleplanner.core.provider.room

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.provider.room.db.AppDatabase

internal fun createInMemoryDatabase(): AppDatabase = Room
    .inMemoryDatabaseBuilder<AppDatabase>()
    .setDriver(BundledSQLiteDriver())
    .build()
