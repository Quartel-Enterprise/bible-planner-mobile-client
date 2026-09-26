package com.quare.bibleplanner.core.plan.domain.usecase

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.plan.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MigratePlanPreferencesToSyncStoreUseCaseTest {
    private val now = 1_700_000_000_000L
    private val startDateKey = longPreferencesKey("plan_start_date")
    private val selectedPlanKey = stringPreferencesKey("selected_reading_plan")
    private val migratedKey = booleanPreferencesKey("plan_preferences_migrated")

    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var dao: FakeSyncedPreferenceDao
    private lateinit var useCase: MigratePlanPreferencesToSyncStoreUseCase

    @Test
    fun `GIVEN legacy preferences WHEN migrating THEN copies them as pending synced values`() = runTest {
        // Given
        prepareScenario(
            mutablePreferencesOf(
                startDateKey to 123L,
                selectedPlanKey to "books",
            ),
        )

        // When
        useCase()

        // Then
        assertEquals(
            mapOf(
                "plan_start_date" to SyncedPreferenceEntity(
                    key = "plan_start_date",
                    value = "123",
                    updatedAt = now,
                    pendingSync = true,
                ),
                "selected_reading_plan" to SyncedPreferenceEntity(
                    key = "selected_reading_plan",
                    value = "books",
                    updatedAt = now,
                    pendingSync = true,
                ),
            ),
            dao.rows.value,
        )
    }

    @Test
    fun `GIVEN legacy preferences WHEN migrating THEN drops the legacy keys and records the migration`() = runTest {
        // Given
        prepareScenario(
            mutablePreferencesOf(
                startDateKey to 123L,
                selectedPlanKey to "books",
            ),
        )

        // When
        useCase()

        // Then
        val preferences = dataStore.data.first()
        assertNull(preferences[startDateKey])
        assertNull(preferences[selectedPlanKey])
        assertEquals(true, preferences[migratedKey])
    }

    @Test
    fun `GIVEN no legacy preferences WHEN migrating THEN writes nothing but still records the migration`() = runTest {
        // Given
        prepareScenario(mutablePreferencesOf())

        // When
        useCase()

        // Then
        assertTrue(dao.rows.value.isEmpty())
        assertEquals(true, dataStore.data.first()[migratedKey])
    }

    @Test
    fun `GIVEN an already migrated store WHEN migrating again THEN leaves everything untouched`() = runTest {
        // Given
        prepareScenario(
            mutablePreferencesOf(
                migratedKey to true,
                startDateKey to 123L,
            ),
        )

        // When
        useCase()

        // Then
        assertTrue(dao.rows.value.isEmpty())
        assertEquals(123L, dataStore.data.first()[startDateKey])
    }

    private fun prepareScenario(preferences: Preferences) {
        dataStore = InMemoryPreferencesDataStore(preferences)
        dao = FakeSyncedPreferenceDao()
        useCase = MigratePlanPreferencesToSyncStoreUseCase(
            planLocalDataSource = PlanLocalDataSource(dataStore),
            syncedPreferenceDao = dao,
            currentTimestampProvider = CurrentTimestampProvider { now },
        )
    }
}
