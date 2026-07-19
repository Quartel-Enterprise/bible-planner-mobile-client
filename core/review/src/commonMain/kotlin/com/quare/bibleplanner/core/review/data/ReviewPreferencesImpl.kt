package com.quare.bibleplanner.core.review.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.review.domain.ReviewPreferences

internal class ReviewPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : ReviewPreferences {
    override suspend fun getFirstEligibleAt(): Long? = dataStore.read(firstEligibleAtKey)

    override suspend fun setFirstEligibleAt(timestamp: Long) = dataStore.write(
        key = firstEligibleAtKey,
        value = timestamp,
    )

    override suspend fun getLastPromptedAt(): Long? = dataStore.read(lastPromptedAtKey)

    override suspend fun setLastPromptedAt(timestamp: Long) = dataStore.write(
        key = lastPromptedAtKey,
        value = timestamp,
    )

    override suspend fun getLastPromptedVersion(): String? = dataStore.read(lastPromptedVersionKey)

    override suspend fun setLastPromptedVersion(version: String) = dataStore.write(
        key = lastPromptedVersionKey,
        value = version,
    )

    private companion object {
        val firstEligibleAtKey = longPreferencesKey("review_first_eligible_at")
        val lastPromptedAtKey = longPreferencesKey("review_last_prompted_at")
        val lastPromptedVersionKey = stringPreferencesKey("review_last_prompted_version")
    }
}
