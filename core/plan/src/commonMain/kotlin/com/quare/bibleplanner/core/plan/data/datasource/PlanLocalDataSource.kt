package com.quare.bibleplanner.core.plan.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import bibleplanner.core.plan.generated.resources.Res
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.datastore.read
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.data.dto.WeekPlanDto
import com.quare.bibleplanner.core.utils.ioDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class PlanLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val startDateKey = longPreferencesKey(PLAN_START_DATE_KEY)
    private val selectedReadingPlanKey = stringPreferencesKey(SELECTED_READING_PLAN_KEY)
    private val migratedKey = booleanPreferencesKey(PLAN_PREFERENCES_MIGRATED_KEY)

    suspend fun hasMigratedPlanPreferences(): Boolean = dataStore.read(migratedKey) ?: false

    suspend fun getLegacyPlanStartTimestamp(): Long? = dataStore.read(startDateKey)

    suspend fun getLegacySelectedReadingPlan(): String? = dataStore.read(selectedReadingPlanKey)

    suspend fun finishPlanPreferencesMigration() {
        dataStore.edit { preferences ->
            preferences -= startDateKey
            preferences -= selectedReadingPlanKey
            preferences[migratedKey] = true
        }
    }

    suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanDto> = withContext(ioDispatcher) {
        val directory = when (readingPlanType) {
            ReadingPlanType.BOOKS -> BOOKS_ORDER_DIRECTORY
            ReadingPlanType.CHRONOLOGICAL -> CHRONOLOGICAL_ORDER_DIRECTORY
        }

        (1..WEEKS_COUNT)
            .map { weekNumber ->
                async {
                    readWeekFromFile(directory, weekNumber)
                }
            }.awaitAll()
            .filterNotNull()
            .sortedBy { it.week }
    }

    private suspend fun readWeekFromFile(
        directory: String,
        weekNumber: Int,
    ): WeekPlanDto? {
        val fileName = "week_${weekNumber.toString().padStart(2, '0')}.json"
        val resourcePath = "files/$directory/$fileName"

        return try {
            val jsonBytes: ByteArray = Res.readBytes(resourcePath)
            val jsonContent = jsonBytes.decodeToString()
            json.decodeFromString<WeekPlanDto>(jsonContent)
        } catch (e: Exception) {
            Logger.e(e) { "Failed to parse reading-plan week $weekNumber from $resourcePath" }
            null
        }
    }

    companion object {
        private const val BOOKS_ORDER_DIRECTORY = "books_order"
        private const val CHRONOLOGICAL_ORDER_DIRECTORY = "chronological_order"
        private const val WEEKS_COUNT = 52
        private const val PLAN_START_DATE_KEY = "plan_start_date"
        private const val SELECTED_READING_PLAN_KEY = "selected_reading_plan"
        private const val PLAN_PREFERENCES_MIGRATED_KEY = "plan_preferences_migrated"
    }
}
