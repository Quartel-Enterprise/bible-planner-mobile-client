package com.quare.bibleplanner.core.devices.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DeviceIdProvider(
    private val dataStore: DataStore<Preferences>,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun getOrCreate(): String = dataStore.data.map { it[deviceIdKey] }.first()
        ?: Uuid.random().toString().also { newId ->
            dataStore.edit { it[deviceIdKey] = newId }
        }

    private companion object {
        val deviceIdKey = stringPreferencesKey("device_installation_id")
    }
}
