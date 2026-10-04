package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SnoozeLoginNudgeUseCaseTest {
    @Test
    fun `GIVEN a nudge never snoozed WHEN snoozing it THEN stores the current timestamp as the snooze start`() =
        runTest {
            // Given
            val now = 555L
            val preferences = FakeLoginNudgePreferences(
                snoozedAt = null,
                dontShowAgain = false,
                firstActionAt = null,
            )
            val useCase = SnoozeLoginNudgeUseCase(
                loginNudgePreferences = preferences,
                currentTimestampProvider = { now },
            )

            // When
            useCase()

            // Then
            assertEquals(now, preferences.getSnoozedAt())
        }
}
