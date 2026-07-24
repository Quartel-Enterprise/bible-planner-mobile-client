package com.quare.bibleplanner.feature.daystudy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyPanelRatioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DayStudyPanelRatioRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : DayStudyPanelRatioRepository {
    override fun observeReadingFraction(): Flow<Float?> = dataStore.data.map { preferences ->
        preferences[READING_FRACTION_KEY]
    }

    override suspend fun setReadingFraction(fraction: Float) = dataStore.write(
        key = READING_FRACTION_KEY,
        value = fraction,
    )

    companion object {
        private val READING_FRACTION_KEY = floatPreferencesKey("day_study_panel_reading_fraction")
    }
}
