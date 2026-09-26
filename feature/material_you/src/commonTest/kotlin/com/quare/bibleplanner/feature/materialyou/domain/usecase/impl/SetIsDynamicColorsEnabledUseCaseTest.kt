package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import com.quare.bibleplanner.feature.materialyou.fake.FakeMaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.fake.FakeSyncedPreferenceDao
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SetIsDynamicColorsEnabledUseCaseTest {
    private lateinit var useCase: SetIsDynamicColorsEnabledUseCase
    private lateinit var repository: FakeMaterialYouRepository
    private lateinit var syncedPreferenceDao: FakeSyncedPreferenceDao

    @Test
    fun `GIVEN theme sync on and a supported device WHEN enabling dynamic colors THEN stores and mirrors the value`() =
        runTest {
            // Given
            prepareScenario(
                isSupported = true,
                isThemeSyncEnabled = true,
            )

            // When
            useCase(true)

            // Then
            assertEquals(listOf(true), repository.writes)
            assertEquals(
                listOf(
                    SyncedPreferenceEntity(
                        key = SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED,
                        value = "true",
                        updatedAt = NOW,
                        pendingSync = true,
                    ),
                ),
                syncedPreferenceDao.localWrites,
            )
        }

    @Test
    fun `GIVEN theme sync off WHEN enabling dynamic colors THEN only stores the value locally`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            isThemeSyncEnabled = false,
        )

        // When
        useCase(true)

        // Then
        assertEquals(listOf(true), repository.writes)
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN a device without Material You WHEN enabling dynamic colors THEN only stores the value locally`() =
        runTest {
            // Given
            prepareScenario(
                isSupported = false,
                isThemeSyncEnabled = true,
            )

            // When
            useCase(true)

            // Then
            assertEquals(listOf(true), repository.writes)
            assertTrue(syncedPreferenceDao.localWrites.isEmpty())
        }

    private fun prepareScenario(
        isSupported: Boolean,
        isThemeSyncEnabled: Boolean,
    ) {
        repository = FakeMaterialYouRepository(isDynamicColorsEnabled = false)
        syncedPreferenceDao = FakeSyncedPreferenceDao(
            mapOf(SyncedPreferenceKeys.THEME_SYNC_ENABLED to isThemeSyncEnabled.toString()),
        )
        useCase = SetIsDynamicColorsEnabledUseCase(
            repository = repository,
            isDynamicColorSupported = { isSupported },
            syncedPreferenceDao = syncedPreferenceDao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
