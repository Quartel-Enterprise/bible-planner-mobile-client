package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual fun createDataStore(): DataStore<Preferences> = commonCreateDataStore { it }
