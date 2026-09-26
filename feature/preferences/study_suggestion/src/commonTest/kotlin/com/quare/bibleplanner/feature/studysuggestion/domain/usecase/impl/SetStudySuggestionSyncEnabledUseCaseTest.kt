package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.fake.FakeStudySuggestionSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetStudySuggestionSyncEnabledUseCaseTest {
    private lateinit var repository: FakeStudySuggestionSettingsRepository
    private lateinit var useCase: SetStudySuggestionSyncEnabledUseCase

    @Test
    fun `GIVEN sync disabled WHEN enabling sync THEN stores sync as enabled`() = runTest {
        // When
        useCase(true)

        // Then
        assertEquals(true, repository.isSyncEnabled.value)
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
        useCase = SetStudySuggestionSyncEnabledUseCase(repository)
    }
}
