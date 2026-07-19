package com.quare.bibleplanner.core.review.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.review.domain.ReviewPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class ReviewPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : ReviewPreferences {
    override suspend fun getFirstEligibleAt(): Long? = dataStore.data
        .map { it[firstEligibleAtKey] }
        .first()

    override suspend fun setFirstEligibleAt(timestamp: Long) {
        dataStore.edit { it[firstEligibleAtKey] = timestamp }
    }

    override suspend fun getLastPromptedAt(): Long? = dataStore.data
        .map { it[lastPromptedAtKey] }
        .first()

    override suspend fun setLastPromptedAt(timestamp: Long) {
        dataStore.edit { it[lastPromptedAtKey] = timestamp }
    }

    override suspend fun getLastPromptedVersion(): String? = dataStore.data
        .map { it[lastPromptedVersionKey] }
        .first()

    override suspend fun setLastPromptedVersion(version: String) {
        dataStore.edit { it[lastPromptedVersionKey] = version }
    }

    private companion object {
        val firstEligibleAtKey = longPreferencesKey("review_first_eligible_at")
        val lastPromptedAtKey = longPreferencesKey("review_last_prompted_at")
        val lastPromptedVersionKey = stringPreferencesKey("review_last_prompted_version")
    }
}
