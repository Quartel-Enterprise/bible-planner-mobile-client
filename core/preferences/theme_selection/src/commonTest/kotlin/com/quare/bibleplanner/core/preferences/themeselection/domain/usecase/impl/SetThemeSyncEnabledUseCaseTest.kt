package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.testing.FakeThemeSelectionRepository
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import com.quare.bibleplanner.core.provider.room.testing.FakeSyncedPreferenceDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SetThemeSyncEnabledUseCaseTest {
    private lateinit var useCase: SetThemeSyncEnabledUseCase
    private lateinit var repository: FakeThemeSelectionRepository
    private lateinit var syncedPreferenceDao: FakeSyncedPreferenceDao

    @Test
    fun `GIVEN material you is supported WHEN enabling sync THEN also publishes the dynamic colors choice`() = runTest {
        // Given
        prepareScenario(isDynamicColorSupported = true)

        // When
        useCase(true)

        // Then
        assertEquals(listOf(true), repository.syncEnabledWrites)
        assertEquals(
            listOf(
                SyncedPreferenceEntity(
                    key = SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED,
                    value = "false",
                    updatedAt = TIMESTAMP,
                    pendingSync = true,
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN material you is not supported WHEN enabling sync THEN does not publish dynamic colors`() = runTest {
        // Given
        prepareScenario(isDynamicColorSupported = false)

        // When
        useCase(true)

        // Then
        assertEquals(listOf(true), repository.syncEnabledWrites)
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN material you is supported WHEN disabling sync THEN does not publish dynamic colors`() = runTest {
        // Given
        prepareScenario(isDynamicColorSupported = true)

        // When
        useCase(false)

        // Then
        assertEquals(listOf(false), repository.syncEnabledWrites)
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    private fun prepareScenario(isDynamicColorSupported: Boolean) {
        repository = FakeThemeSelectionRepository(
            initialTheme = Theme.SYSTEM,
            initialContrast = ContrastType.Standard,
            initialSyncEnabled = false,
        )
        syncedPreferenceDao = FakeSyncedPreferenceDao(emptyMap())
        useCase = SetThemeSyncEnabledUseCase(
            repository = repository,
            getIsDynamicColorsEnabledFlow = { flowOf(false) },
            isDynamicColorSupported = { isDynamicColorSupported },
            syncedPreferenceDao = syncedPreferenceDao,
            currentTimestampProvider = { TIMESTAMP },
        )
    }

    private companion object {
        const val TIMESTAMP = 1_000L
    }
}
