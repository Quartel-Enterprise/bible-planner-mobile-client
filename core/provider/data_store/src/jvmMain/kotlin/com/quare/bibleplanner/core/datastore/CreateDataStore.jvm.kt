package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.quare.bibleplanner.core.utils.migrateLegacyFileIfPresent
import com.quare.bibleplanner.core.utils.resolveAppDataDirectory
import java.io.File

actual fun createDataStore(): DataStore<Preferences> = commonCreateDataStore { dataStoreFileName ->
    resolveDataStoreFile(dataStoreFileName).absolutePath
}

private fun resolveDataStoreFile(dataStoreFileName: String): File {
    val targetFile = File(resolveAppDataDirectory(), dataStoreFileName)
    migrateLegacyFileIfPresent(
        legacyFile = File(System.getProperty("user.dir"), dataStoreFileName),
        targetFile = targetFile,
    )
    return targetFile
}
