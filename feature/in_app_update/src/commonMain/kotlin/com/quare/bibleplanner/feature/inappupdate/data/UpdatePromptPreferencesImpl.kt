package com.quare.bibleplanner.feature.inappupdate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptPreferences

internal class UpdatePromptPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : UpdatePromptPreferences {
    override suspend fun getLastPromptedAt(): Long? = dataStore.read(lastPromptedAtKey)

    override suspend fun setLastPromptedAt(timestamp: Long) = dataStore.write(
        key = lastPromptedAtKey,
        value = timestamp,
    )

    private companion object {
        val lastPromptedAtKey = longPreferencesKey("update_prompt_last_prompted_at")
    }
}
