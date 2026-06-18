package com.quare.bibleplanner.core.loginnudge.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class LoginNudgePreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : LoginNudgePreferences {
    override suspend fun getSnoozedAt(): Long? = dataStore.data
        .map { it[snoozedAtKey] }
        .first()

    override suspend fun setSnoozedAt(timestamp: Long) {
        dataStore.edit { it[snoozedAtKey] = timestamp }
    }

    override suspend fun isDontShowAgain(): Boolean = dataStore.data
        .map { it[dontShowAgainKey] == true }
        .first()

    override suspend fun setDontShowAgain() {
        dataStore.edit { it[dontShowAgainKey] = true }
    }

    override suspend fun getFirstActionAt(): Long? = dataStore.data
        .map { it[firstActionAtKey] }
        .first()

    override suspend fun setFirstActionAt(timestamp: Long) {
        dataStore.edit { it[firstActionAtKey] = timestamp }
    }

    private companion object {
        val snoozedAtKey = longPreferencesKey("login_nudge_snoozed_at")
        val dontShowAgainKey = booleanPreferencesKey("login_nudge_dont_show_again")
        val firstActionAtKey = longPreferencesKey("login_nudge_first_action_at")
    }
}
