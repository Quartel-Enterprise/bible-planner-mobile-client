package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.Flow
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
            listOf(Triple(SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED, "false", TIMESTAMP)),
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
        repository = FakeThemeSelectionRepository()
        syncedPreferenceDao = FakeSyncedPreferenceDao()
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

private class FakeThemeSelectionRepository : ThemeSelectionRepository {
    val syncEnabledWrites = mutableListOf<Boolean>()

    override suspend fun setThemeSyncEnabled(enabled: Boolean) {
        syncEnabledWrites += enabled
    }

    override fun getThemeFlow(): Flow<Theme> = error("unused")

    override suspend fun setTheme(theme: Theme) = error("unused")

    override fun getContrastTypeFlow(): Flow<ContrastType> = error("unused")

    override suspend fun setContrastType(contrastType: ContrastType) = error("unused")

    override fun getThemeSyncEnabledFlow(): Flow<Boolean> = error("unused")

    override fun observeSyncedTheme(): Flow<Theme?> = error("unused")

    override fun observeSyncedContrast(): Flow<ContrastType?> = error("unused")

    override suspend fun applySyncedTheme(theme: Theme) = error("unused")

    override suspend fun applySyncedContrast(contrastType: ContrastType) = error("unused")
}

private class FakeSyncedPreferenceDao : SyncedPreferenceDao() {
    val localWrites = mutableListOf<Triple<String, String, Long>>()

    override suspend fun setLocal(
        key: String,
        value: String,
        updatedAt: Long,
    ) {
        localWrites += Triple(key, value, updatedAt)
    }

    override fun observeValue(key: String): Flow<String?> = error("unused")

    override fun getPendingFlow(): Flow<List<SyncedPreferenceEntity>> = error("unused")

    override suspend fun getPending(): List<SyncedPreferenceEntity> = error("unused")

    override suspend fun markSynced(
        key: String,
        syncedUpdatedAt: Long,
    ) = error("unused")

    override suspend fun seedProvisional(
        key: String,
        value: String,
    ) = error("unused")

    override suspend fun adoptProvisional(now: Long) = error("unused")

    override suspend fun deleteByKeys(keys: List<String>) = error("unused")

    override suspend fun deleteAll() = error("unused")

    override suspend fun updateFromRemote(
        key: String,
        value: String,
        remoteUpdatedAt: Long,
    ): Int = error("unused")

    override suspend fun insertIfAbsent(entity: SyncedPreferenceEntity) = error("unused")
}
