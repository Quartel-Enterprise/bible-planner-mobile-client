package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.fake.FakeThemeSelectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetThemeOptionFlowUseCaseTest {
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
    fun `GIVEN a stored theme WHEN reading it THEN emits the stored theme`() = runTest {
        // When
        val theme = GetThemeOptionFlowUseCase(repository)().first()

        // Then
        assertEquals(Theme.DARK, theme)
    }
}
