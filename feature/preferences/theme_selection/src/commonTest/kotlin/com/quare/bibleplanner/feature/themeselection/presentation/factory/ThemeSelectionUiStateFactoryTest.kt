package com.quare.bibleplanner.feature.themeselection.presentation.factory

import bibleplanner.feature.preferences.theme_selection.generated.resources.Res
import bibleplanner.feature.preferences.theme_selection.generated.resources.dark_title
import bibleplanner.feature.preferences.theme_selection.generated.resources.light_title
import bibleplanner.feature.preferences.theme_selection.generated.resources.system_title
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.GetContrastTypeFlowUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.GetThemeOptionFlowUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.GetThemeSyncEnabledFlowUseCase
import com.quare.bibleplanner.feature.themeselection.presentation.FakeThemeSelectionRepository
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ThemeSelectionUiStateFactoryTest {
    private lateinit var factory: ThemeSelectionUiStateFactory

    @Test
    fun `GIVEN the dark theme stored WHEN creating the state THEN marks only the dark option as active`() = runTest {
        // Given
        prepareScenario(theme = Theme.DARK)

        // When
        val state = factory.create().first()

        // Then
        assertEquals(
            listOf(
                Res.string.light_title to false,
                Res.string.dark_title to true,
                Res.string.system_title to false,
            ),
            state.options.map { it.title to it.isActive },
        )
    }

    @Test
    fun `GIVEN a logged in user with sync on WHEN creating the state THEN exposes the stored preferences`() = runTest {
        // Given
        prepareScenario(
            contrast = ContrastType.High,
            isSyncEnabled = true,
            userId = "user-1",
            isDynamicColorsEnabled = true,
        )

        // When
        val state = factory.create().first()

        // Then
        assertEquals(ContrastType.High, state.selectedContrast)
        assertTrue(state.isSyncEnabled)
        assertTrue(state.isLoggedIn)
        assertEquals(true, state.isMaterialYouToggleOn)
    }

    @Test
    fun `GIVEN a logged out user WHEN creating the state THEN reports the user as logged out`() = runTest {
        // Given
        prepareScenario(userId = null)

        // When
        val state = factory.create().first()

        // Then
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `GIVEN material you is not supported WHEN creating the state THEN hides the material you toggle`() = runTest {
        // Given
        prepareScenario(
            isDynamicColorSupported = false,
            isDynamicColorsEnabled = true,
        )

        // When
        val state = factory.create().first()

        // Then
        assertNull(state.isMaterialYouToggleOn)
    }

    private fun prepareScenario(
        theme: Theme = Theme.SYSTEM,
        contrast: ContrastType = ContrastType.Standard,
        isSyncEnabled: Boolean = false,
        userId: String? = "user-1",
        isDynamicColorSupported: Boolean = true,
        isDynamicColorsEnabled: Boolean = false,
    ) {
        val repository = FakeThemeSelectionRepository(
            initialTheme = theme,
            initialContrast = contrast,
            initialSyncEnabled = isSyncEnabled,
        )
        factory = ThemeSelectionUiStateFactory(
            getThemeOptionFlow = GetThemeOptionFlowUseCase(repository),
            getIsDynamicColorsEnabledFlow = { flowOf(isDynamicColorsEnabled) },
            getContrastTypeFlow = GetContrastTypeFlowUseCase(repository),
            isDynamicColorSupported = { isDynamicColorSupported },
            getThemeSyncEnabledFlow = GetThemeSyncEnabledFlowUseCase(repository),
            observeAuthenticatedUserId = { flowOf(userId) },
        )
    }
}
