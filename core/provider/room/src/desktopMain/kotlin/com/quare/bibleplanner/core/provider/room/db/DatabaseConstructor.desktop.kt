package com.quare.bibleplanner.core.provider.room.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.provider.room.utils.DatabaseUtils
import java.io.File

private const val APP_DATA_DIRECTORY_NAME = "com.quare.bibleplanner"

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = resolveDatabaseFile()
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        ).setDriver(BundledSQLiteDriver())
}

private fun resolveDatabaseFile(): File {
    val dataDirectory = resolveAppDataDirectory()
    dataDirectory.mkdirs()
    val dbFile = File(dataDirectory, DatabaseUtils.PATH)
    migrateLegacyDatabaseIfPresent(dbFile)
    return dbFile
}

private fun resolveAppDataDirectory(): File {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("mac") -> File(userHome, "Library/Application Support/$APP_DATA_DIRECTORY_NAME")
        osName.contains("win") -> File(windowsAppDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
        else -> File(linuxDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
    }
}

private fun windowsAppDataRoot(userHome: String): String = System.getenv("APPDATA")?.takeIf(String::isNotBlank)
    ?: File(userHome, "AppData/Roaming").absolutePath

private fun linuxDataRoot(userHome: String): String = System.getenv("XDG_DATA_HOME")?.takeIf(String::isNotBlank)
    ?: File(userHome, ".local/share").absolutePath

private fun migrateLegacyDatabaseIfPresent(targetFile: File) {
    val legacyFile = File(System.getProperty("java.io.tmpdir"), DatabaseUtils.PATH)
    if (!legacyFile.exists() || targetFile.exists()) return
    listOf("", "-wal", "-shm").forEach { suffix ->
        val source = File(legacyFile.parentFile, legacyFile.name + suffix)
        if (source.exists()) {
            source.copyTo(File(targetFile.parentFile, targetFile.name + suffix))
            source.delete()
        }
    }
}
