package com.quare.bibleplanner.core.provider.supabase.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class DataStoreSessionAuditStore(
    private val dataStore: DataStore<Preferences>,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SessionAuditStore {
    override suspend fun recordSaved() = dataStore.write(
        key = lastSavedAtKey,
        value = currentTimestampProvider.getCurrentTimestamp(),
    )

    override suspend fun recordDeleted() = dataStore.write(
        key = lastDeletedAtKey,
        value = currentTimestampProvider.getCurrentTimestamp(),
    )

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
