package com.quare.bibleplanner.feature.onboardingstartdate.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.quare.bibleplanner.feature.onboardingstartdate.domain.repository.OnboardingStartDateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OnboardingStartDateRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : OnboardingStartDateRepository {

    override fun getDontShowAgainFlow(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(DONT_SHOW_AGAIN_KEY)] ?: false
    }

    override suspend fun setDontShowAgain(dontShow: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(DONT_SHOW_AGAIN_KEY)] = dontShow
        }
    }

    companion object {
        private const val DONT_SHOW_AGAIN_KEY = "onboarding_start_date_dont_show_again"
    }
}

