package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.fake.FakeThemeSelectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetContrastTypeFlowUseCaseTest {
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
    fun `GIVEN a stored contrast WHEN reading it THEN emits the stored contrast`() = runTest {
        // When
        val contrast = GetContrastTypeFlowUseCase(repository)().first()

        // Then
        assertEquals(ContrastType.High, contrast)
    }
}
