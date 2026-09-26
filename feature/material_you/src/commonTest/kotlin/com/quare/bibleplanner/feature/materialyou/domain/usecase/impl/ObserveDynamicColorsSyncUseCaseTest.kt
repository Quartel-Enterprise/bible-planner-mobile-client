package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.materialyou.fake.FakeMaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.fake.FakeSyncedPreferenceDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ObserveDynamicColorsSyncUseCaseTest {
    private lateinit var useCase: ObserveDynamicColorsSyncUseCase
    private lateinit var repository: FakeMaterialYouRepository

    @Test
    fun `GIVEN theme sync on and a synced value WHEN observing THEN applies the synced value locally`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            syncedValues = mapOf(
                SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true",
                SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED to "true",
            ),
        )

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals(listOf(true), repository.writes)
    }

    @Test
    fun `GIVEN theme sync off WHEN observing THEN ignores the synced value`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            syncedValues = mapOf(
                SyncedPreferenceKeys.THEME_SYNC_ENABLED to "false",
                SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED to "true",
            ),
        )

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun `GIVEN theme sync on without a synced value WHEN observing THEN applies nothing`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            syncedValues = mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true"),
        )

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun `GIVEN a device without Material You WHEN observing THEN returns without applying anything`() = runTest {
        // Given
        prepareScenario(
            isSupported = false,
            syncedValues = mapOf(
                SyncedPreferenceKeys.THEME_SYNC_ENABLED to "true",
                SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED to "true",
            ),
        )

        // When
        useCase()

        // Then
        assertTrue(repository.writes.isEmpty())
    }

    private fun prepareScenario(
        isSupported: Boolean,
        syncedValues: Map<String, String>,
    ) {
        repository = FakeMaterialYouRepository(isDynamicColorsEnabled = false)
        useCase = ObserveDynamicColorsSyncUseCase(
            repository = repository,
            isDynamicColorSupported = { isSupported },
            syncedPreferenceDao = FakeSyncedPreferenceDao(syncedValues),
        )
    }
}
