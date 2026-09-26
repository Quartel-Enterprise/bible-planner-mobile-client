package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.fake.FakeThemeSelectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GetThemeSyncEnabledFlowUseCaseTest {
    private lateinit var repository: FakeThemeSelectionRepository

    @BeforeTest
    fun setUp() {
        repository = FakeThemeSelectionRepository(
            initialTheme = Theme.DARK,
            initialContrast = ContrastType.High,
            initialSyncEnabled = true,
        )
    }

    @Test
    fun `GIVEN theme sync enabled WHEN reading it THEN emits true`() = runTest {
        // When
        val isSyncEnabled = GetThemeSyncEnabledFlowUseCase(repository)().first()

        // Then
        assertTrue(isSyncEnabled)
    }
}
