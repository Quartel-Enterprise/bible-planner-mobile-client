package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), DatabaseUtils.PATH)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
