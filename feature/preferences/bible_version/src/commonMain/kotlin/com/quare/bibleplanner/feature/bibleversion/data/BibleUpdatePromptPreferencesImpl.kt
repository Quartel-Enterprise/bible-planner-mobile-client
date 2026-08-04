package com.quare.bibleplanner.feature.bibleversion.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences

internal class BibleUpdatePromptPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : BibleUpdatePromptPreferences {
    override suspend fun getLastDismissedAt(): Long? = dataStore.read(lastDismissedAtKey)

    override suspend fun setLastDismissedAt(timestamp: Long) = dataStore.write(
        key = lastDismissedAtKey,
        value = timestamp,
    )

    private companion object {
        val lastDismissedAtKey = longPreferencesKey("bible_update_prompt_last_dismissed_at")
    }
}
