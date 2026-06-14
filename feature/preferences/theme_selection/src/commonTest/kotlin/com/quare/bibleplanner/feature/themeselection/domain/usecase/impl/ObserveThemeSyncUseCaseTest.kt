package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObserveThemeSyncUseCaseTest {
    @Test
    fun `applies synced theme and contrast when sync is enabled`() = runTest {
        val repository = FakeThemeSelectionRepository(
            syncEnabled = true,
            syncedTheme = Theme.DARK,
            syncedContrast = ContrastType.High,
        )

        ObserveThemeSyncUseCase(repository).invoke()

        assertEquals(Theme.DARK, repository.appliedTheme)
        assertEquals(ContrastType.High, repository.appliedContrast)
    }

    @Test
    fun `applies nothing when sync is disabled`() = runTest {
        val repository = FakeThemeSelectionRepository(
            syncEnabled = false,
            syncedTheme = Theme.DARK,
            syncedContrast = ContrastType.High,
        )

        ObserveThemeSyncUseCase(repository).invoke()

        assertNull(repository.appliedTheme)
        assertNull(repository.appliedContrast)
    }

    @Test
    fun `skips a missing synced value`() = runTest {
        val repository = FakeThemeSelectionRepository(
            syncEnabled = true,
            syncedTheme = null,
            syncedContrast = ContrastType.Medium,
        )

        ObserveThemeSyncUseCase(repository).invoke()

        assertNull(repository.appliedTheme)
        assertEquals(ContrastType.Medium, repository.appliedContrast)
    }

    private class FakeThemeSelectionRepository(
        private val syncEnabled: Boolean,
        private val syncedTheme: Theme?,
        private val syncedContrast: ContrastType?,
    ) : ThemeSelectionRepository {
        var appliedTheme: Theme? = null
        var appliedContrast: ContrastType? = null

        override fun getThemeSyncEnabledFlow(): Flow<Boolean> = flowOf(syncEnabled)

        override fun observeSyncedTheme(): Flow<Theme?> = flowOf(syncedTheme)

        override fun observeSyncedContrast(): Flow<ContrastType?> = flowOf(syncedContrast)

        override suspend fun applySyncedTheme(theme: Theme) {
            appliedTheme = theme
        }

        override suspend fun applySyncedContrast(contrastType: ContrastType) {
            appliedContrast = contrastType
        }

        override fun getThemeFlow(): Flow<Theme> = flowOf(Theme.SYSTEM)

        override suspend fun setTheme(theme: Theme) = Unit

        override fun getContrastTypeFlow(): Flow<ContrastType> = flowOf(ContrastType.Standard)

        override suspend fun setContrastType(contrastType: ContrastType) = Unit

        override suspend fun setThemeSyncEnabled(enabled: Boolean) = Unit
    }
}
