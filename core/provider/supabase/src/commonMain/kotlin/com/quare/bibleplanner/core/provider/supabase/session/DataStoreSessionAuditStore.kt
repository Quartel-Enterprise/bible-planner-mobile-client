package com.quare.bibleplanner.core.provider.supabase.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class DataStoreSessionAuditStore(
    private val dataStore: DataStore<Preferences>,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SessionAuditStore {
    override suspend fun recordSaved() {
        val now = currentTimestampProvider.getCurrentTimestamp()
        dataStore.edit { it[lastSavedAtKey] = now }
    }

    override suspend fun recordDeleted() {
        val now = currentTimestampProvider.getCurrentTimestamp()
        dataStore.edit { it[lastDeletedAtKey] = now }
    }

    override suspend fun getAudit(): SessionAudit = dataStore.data
        .map { preferences ->
            SessionAudit(
                lastSavedAtMillis = preferences[lastSavedAtKey],
                lastDeletedAtMillis = preferences[lastDeletedAtKey],
            )
        }.first()

    private companion object {
        val lastSavedAtKey = longPreferencesKey("supabase_session_last_saved_at")
        val lastDeletedAtKey = longPreferencesKey("supabase_session_last_deleted_at")
    }
}
