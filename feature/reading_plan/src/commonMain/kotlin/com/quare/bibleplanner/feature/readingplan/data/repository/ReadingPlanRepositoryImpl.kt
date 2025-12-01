package com.quare.bibleplanner.feature.readingplan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.data.mapper.ReadingPlanPreferenceMapper
import com.quare.bibleplanner.feature.readingplan.domain.repository.ReadingPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ReadingPlanRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: ReadingPlanPreferenceMapper,
) : ReadingPlanRepository {
    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = dataStore.data.map { value ->
        mapper.mapPreferenceToModel(value[stringPreferencesKey(SELECTED_READING_PLAN_KEY)])
    }

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) {
        dataStore.edit {
            it[stringPreferencesKey(SELECTED_READING_PLAN_KEY)] = mapper.mapModelToPreference(readingPlanType)
        }
    }

    companion object {
        private const val SELECTED_READING_PLAN_KEY = "selected_reading_plan"
    }
}
