package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.testing.FakeThemeSelectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObserveThemeSyncUseCaseTest {
    private lateinit var repository: FakeThemeSelectionRepository

    @Test
    fun `GIVEN sync enabled WHEN observing the theme sync THEN applies the synced theme and contrast`() = runTest {
        // Given
        prepareScenario(
            syncEnabled = true,
            syncedTheme = Theme.DARK,
            syncedContrast = ContrastType.High,
        )

        // When
        backgroundScope.launch { ObserveThemeSyncUseCase(repository).invoke() }
        runCurrent()

        // Then
        assertEquals(listOf(Theme.DARK), repository.appliedThemes)
        assertEquals(listOf(ContrastType.High), repository.appliedContrasts)
    }

    @Test
    fun `GIVEN sync disabled WHEN observing the theme sync THEN applies nothing`() = runTest {
        // Given
        prepareScenario(
            syncEnabled = false,
            syncedTheme = Theme.DARK,
            syncedContrast = ContrastType.High,
        )

        // When
        backgroundScope.launch { ObserveThemeSyncUseCase(repository).invoke() }
        runCurrent()

        // Then
        assertTrue(repository.appliedThemes.isEmpty())
        assertTrue(repository.appliedContrasts.isEmpty())
    }

    @Test
    fun `GIVEN a missing synced theme WHEN observing the theme sync THEN skips it`() = runTest {
        // Given
        prepareScenario(
            syncEnabled = true,
            syncedTheme = null,
            syncedContrast = ContrastType.Medium,
        )

        // When
        backgroundScope.launch { ObserveThemeSyncUseCase(repository).invoke() }
        runCurrent()

        // Then
        assertTrue(repository.appliedThemes.isEmpty())
        assertEquals(listOf(ContrastType.Medium), repository.appliedContrasts)
    }

    private fun prepareScenario(
        syncEnabled: Boolean,
        syncedTheme: Theme?,
        syncedContrast: ContrastType?,
    ) {
        repository = FakeThemeSelectionRepository(
            initialTheme = Theme.SYSTEM,
            initialContrast = ContrastType.Standard,
            initialSyncEnabled = syncEnabled,
        )
        repository.syncedTheme.value = syncedTheme
        repository.syncedContrast.value = syncedContrast
    }
}
