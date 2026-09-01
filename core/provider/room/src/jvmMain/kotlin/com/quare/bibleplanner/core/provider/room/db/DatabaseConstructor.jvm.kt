package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils
import com.quare.bibleplanner.core.utils.migrateLegacyFileIfPresent
import com.quare.bibleplanner.core.utils.resolveAppDataDirectory
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = resolveDatabaseFile()
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        ).setDriver(BundledSQLiteDriver())
}

private fun resolveDatabaseFile(): File {
    val dbFile = File(resolveAppDataDirectory(), DatabaseUtils.PATH)
    migrateLegacyDatabaseIfPresent(dbFile)
    return dbFile
}

private fun migrateLegacyDatabaseIfPresent(targetFile: File) {
    val legacyFile = File(System.getProperty("java.io.tmpdir"), DatabaseUtils.PATH)
    if (!legacyFile.exists() || targetFile.exists()) return
    listOf("", "-wal", "-shm").forEach { suffix ->
        migrateLegacyFileIfPresent(
            legacyFile = File(legacyFile.parentFile, legacyFile.name + suffix),
            targetFile = File(targetFile.parentFile, targetFile.name + suffix),
        )
    }
}
