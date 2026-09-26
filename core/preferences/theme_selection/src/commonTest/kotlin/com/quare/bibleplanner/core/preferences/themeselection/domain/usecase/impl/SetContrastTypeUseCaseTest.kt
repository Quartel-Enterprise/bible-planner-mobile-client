package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.fake.FakeThemeSelectionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetContrastTypeUseCaseTest {
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
    fun `GIVEN a stored contrast WHEN choosing another THEN stores the new contrast`() = runTest {
        // When
        SetContrastTypeUseCase(repository)(ContrastType.Medium)

        // Then
        assertEquals(ContrastType.Medium, repository.contrast.value)
    }
}
