package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SnoozeLoginNudgeUseCaseTest {
    @Test
    fun `stores the current timestamp as the snooze start`() = runTest {
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

        useCase()

        assertEquals(now, preferences.getSnoozedAt())
    }
}
