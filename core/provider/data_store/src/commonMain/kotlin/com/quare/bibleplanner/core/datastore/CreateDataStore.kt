package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

expect fun createDataStore(): DataStore<Preferences>

private const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

fun commonCreateDataStore(producePath: (dataStoreFileName: String) -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath(DATA_STORE_FILE_NAME).toPath() },
    )
