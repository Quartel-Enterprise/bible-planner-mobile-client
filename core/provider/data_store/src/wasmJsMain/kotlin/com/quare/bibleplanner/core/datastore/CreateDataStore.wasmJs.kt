package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer

private const val DATA_STORE_NAME = "prefs"

actual fun createDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
    storage = WebLocalStorage(
        serializer = PreferencesSerializer,
        name = DATA_STORE_NAME,
    ),
)
