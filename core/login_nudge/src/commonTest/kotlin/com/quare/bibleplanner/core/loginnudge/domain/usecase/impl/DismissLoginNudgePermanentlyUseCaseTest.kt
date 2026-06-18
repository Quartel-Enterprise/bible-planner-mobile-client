package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class DismissLoginNudgePermanentlyUseCaseTest {
    @Test
    fun `marks the nudge as permanently dismissed`() = runTest {
        val preferences = FakeLoginNudgePreferences(
            snoozedAt = null,
            dontShowAgain = false,
            firstActionAt = null,
        )
        val useCase = DismissLoginNudgePermanentlyUseCase(preferences)

        useCase()

        assertTrue(preferences.isDontShowAgain())
    }
}
