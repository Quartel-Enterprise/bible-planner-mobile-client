package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.fake.FakeStudySuggestionSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveStudySuggestionSettingsUseCaseTest {
    private lateinit var repository: FakeStudySuggestionSettingsRepository
    private lateinit var useCase: ObserveStudySuggestionSettingsUseCase

    @Test
    fun `GIVEN stored settings WHEN observing THEN emits the repository settings`() = runTest {
        // When
        val settings = useCase().first()

        // Then
        assertEquals(
            StudySuggestionSettingsModel(
                isEnabled = true,
                mode = StudySuggestionMode.DIALOG,
            ),
            settings,
        )
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
        useCase = ObserveStudySuggestionSettingsUseCase(repository)
    }
}
