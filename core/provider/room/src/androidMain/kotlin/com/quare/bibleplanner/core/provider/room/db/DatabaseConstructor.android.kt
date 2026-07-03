package com.quare.bibleplanner.core.provider.room.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DatabaseUtils.PATH)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
