package com.quare.bibleplanner.core.devices.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import kotlin.uuid.Uuid

internal class DeviceIdProvider(
    private val dataStore: DataStore<Preferences>,
) {
    private val deviceIdKey = stringPreferencesKey("device_installation_id")

    suspend fun getOrCreate(): String = dataStore.read(deviceIdKey)
        ?: Uuid.random().toString().also { newId ->
            dataStore.write(
                key = deviceIdKey,
                value = newId,
            )
        }
}
