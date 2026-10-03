package com.quare.bibleplanner.core.installattribution.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.utils.orFalse
import kotlin.uuid.Uuid

internal class InstallAttributionLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val installIdKey = stringPreferencesKey("install_attribution_install_id")
    private val reportedKey = booleanPreferencesKey("install_attribution_reported")

    suspend fun isReported(): Boolean = dataStore.read(reportedKey).orFalse()

    suspend fun markReported() {
        dataStore.write(
            key = reportedKey,
            value = true,
        )
    }

    suspend fun getOrCreateInstallId(): String = dataStore.read(installIdKey)
        ?: Uuid.random().toString().also { newId ->
            dataStore.write(
                key = installIdKey,
                value = newId,
            )
        }
}
