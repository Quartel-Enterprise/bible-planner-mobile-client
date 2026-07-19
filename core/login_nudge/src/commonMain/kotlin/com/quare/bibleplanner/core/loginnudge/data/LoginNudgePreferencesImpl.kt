package com.quare.bibleplanner.core.loginnudge.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences

internal class LoginNudgePreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : LoginNudgePreferences {
    override suspend fun getSnoozedAt(): Long? = dataStore.read(snoozedAtKey)

    override suspend fun setSnoozedAt(timestamp: Long) = dataStore.write(
        key = snoozedAtKey,
        value = timestamp,
    )

    override suspend fun isDontShowAgain(): Boolean = dataStore.read(dontShowAgainKey) == true

    override suspend fun setDontShowAgain() = dataStore.write(
        key = dontShowAgainKey,
        value = true,
    )

    override suspend fun getFirstActionAt(): Long? = dataStore.read(firstActionAtKey)

    override suspend fun setFirstActionAt(timestamp: Long) = dataStore.write(
        key = firstActionAtKey,
        value = timestamp,
    )

    private companion object {
        val snoozedAtKey = longPreferencesKey("login_nudge_snoozed_at")
        val dontShowAgainKey = booleanPreferencesKey("login_nudge_dont_show_again")
        val firstActionAtKey = longPreferencesKey("login_nudge_first_action_at")
    }
}
