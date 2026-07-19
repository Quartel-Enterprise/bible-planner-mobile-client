package com.quare.bibleplanner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

suspend fun <T> DataStore<Preferences>.read(key: Preferences.Key<T>): T? = data
    .map { preferences -> preferences[key] }
    .first()

suspend fun <T> DataStore<Preferences>.write(
    key: Preferences.Key<T>,
    value: T,
) {
    edit { preferences -> preferences[key] = value }
}
