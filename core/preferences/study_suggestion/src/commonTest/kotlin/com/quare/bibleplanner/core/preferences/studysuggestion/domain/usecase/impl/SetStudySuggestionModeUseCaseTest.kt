package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.fake.FakeStudySuggestionSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetStudySuggestionModeUseCaseTest {
    private lateinit var repository: FakeStudySuggestionSettingsRepository
    private lateinit var useCase: SetStudySuggestionModeUseCase

    @Test
    fun `GIVEN dialog mode WHEN picking banner mode THEN stores the banner mode`() = runTest {
        // When
        useCase(StudySuggestionMode.BANNER)

        // Then
        assertEquals(StudySuggestionMode.BANNER, repository.settings.value.mode)
    }

    @BeforeTest
    fun setUp() {
        repository = FakeStudySuggestionSettingsRepository(
            settings = StudySuggestionSettingsModel(
                isEnabled = true,
                mode = StudySuggestionMode.DIALOG,
            ),
            isSyncEnabled = false,
        )
        useCase = SetStudySuggestionModeUseCase(repository)
    }
}
