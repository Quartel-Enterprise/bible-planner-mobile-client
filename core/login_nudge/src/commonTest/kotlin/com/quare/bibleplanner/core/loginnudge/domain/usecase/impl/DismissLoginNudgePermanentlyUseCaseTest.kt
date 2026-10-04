package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class DismissLoginNudgePermanentlyUseCaseTest {
    @Test
    fun `GIVEN a nudge not dismissed WHEN dismissing it permanently THEN marks it as permanently dismissed`() =
        runTest {
            // Given
            val preferences = FakeLoginNudgePreferences(
                snoozedAt = null,
                dontShowAgain = false,
                firstActionAt = null,
            )
            val useCase = DismissLoginNudgePermanentlyUseCase(preferences)

            // When
            useCase()

            // Then
            assertTrue(preferences.isDontShowAgain())
        }
}
