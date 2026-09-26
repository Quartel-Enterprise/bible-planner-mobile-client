package com.quare.bibleplanner.core.devices.fake

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakePreferencesDataStore(
    initialPreferences: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val preferences = MutableStateFlow(initialPreferences)

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        transform(preferences.value).also { preferences.value = it }
}
