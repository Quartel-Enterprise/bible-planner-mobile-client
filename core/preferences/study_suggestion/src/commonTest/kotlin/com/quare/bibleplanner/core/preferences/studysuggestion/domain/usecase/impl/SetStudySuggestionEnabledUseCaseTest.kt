package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.fake.FakeStudySuggestionSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetStudySuggestionEnabledUseCaseTest {
    private lateinit var repository: FakeStudySuggestionSettingsRepository
    private lateinit var useCase: SetStudySuggestionEnabledUseCase

    @Test
    fun `GIVEN suggestions enabled WHEN disabling them THEN stores the disabled setting`() = runTest {
        // When
        useCase(false)

        // Then
        assertEquals(false, repository.settings.value.isEnabled)
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
        useCase = SetStudySuggestionEnabledUseCase(repository)
    }
}
