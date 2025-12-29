package com.quare.bibleplanner.core.plan.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import bibleplanner.core.plan.generated.resources.Res
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.data.dto.WeekPlanDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class PlanLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val startDateKey = longPreferencesKey(PLAN_START_DATE_KEY)

    fun getPlanStartTimeStamp(): Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[startDateKey]
        }

    suspend fun removeLocalDate() {
        dataStore.edit { preferences ->
            preferences.remove(startDateKey)
        }
    }

    suspend fun setPlanStartTimestamp(epoch: Long) {
        dataStore.edit { preferences ->
            preferences[startDateKey] = epoch
        }
    }

    suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanDto> = withContext(Dispatchers.IO) {
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
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val BOOKS_ORDER_DIRECTORY = "books_order"
        private const val CHRONOLOGICAL_ORDER_DIRECTORY = "chronological_order"
        private const val WEEKS_COUNT = 52
        private const val PLAN_START_DATE_KEY = "plan_start_date"
    }
}
