package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.fake.FakeStudySuggestionSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetStudySuggestionSyncEnabledFlowUseCaseTest {
    private lateinit var repository: FakeStudySuggestionSettingsRepository
    private lateinit var useCase: GetStudySuggestionSyncEnabledFlowUseCase

    @Test
    fun `GIVEN sync disabled WHEN observing the sync flag THEN emits false`() = runTest {
        // When
        val isSyncEnabled = useCase().first()

        // Then
        assertEquals(false, isSyncEnabled)
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
        useCase = GetStudySuggestionSyncEnabledFlowUseCase(repository)
    }
}
