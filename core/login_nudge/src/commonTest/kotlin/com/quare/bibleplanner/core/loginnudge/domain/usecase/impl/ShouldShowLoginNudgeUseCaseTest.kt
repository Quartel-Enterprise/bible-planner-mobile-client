package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShouldShowLoginNudgeUseCaseTest {
    @Test
    fun `returns false when the user is authenticated`() = runTest {
        assertFalse(useCase(userId = "user-1")())
    }

    @Test
    fun `returns false when permanently dismissed`() = runTest {
        assertFalse(useCase(dontShowAgain = true)())
    }

    @Test
    fun `returns false when offline`() = runTest {
        assertFalse(useCase(isOnline = false)())
    }

    @Test
    fun `returns true when never snoozed`() = runTest {
        assertTrue(useCase(snoozedAt = null)())
    }

    @Test
    fun `returns false within the snooze window`() = runTest {
        assertFalse(useCase(snoozedAt = NOW - SNOOZE_MILLIS + 1)())
    }

    @Test
    fun `returns true once the snooze window elapsed`() = runTest {
        assertTrue(useCase(snoozedAt = NOW - SNOOZE_MILLIS)())
    }

    @Test
    fun `returns false within the first action grace period`() = runTest {
        assertFalse(useCase(firstActionAt = NOW - GRACE_MILLIS + 1)())
    }

    @Test
    fun `stamps the first action and suppresses on the very first evaluation`() = runTest {
        val preferences = FakeLoginNudgePreferences(
            snoozedAt = null,
            dontShowAgain = false,
            firstActionAt = null,
        )

        val result = useCase(preferences)()

        assertFalse(result)
        assertEquals(NOW, preferences.getFirstActionAt())
    }

    private fun useCase(
        userId: String? = null,
        isOnline: Boolean = true,
        snoozedAt: Long? = null,
        dontShowAgain: Boolean = false,
        firstActionAt: Long? = NOW - GRACE_MILLIS,
    ): ShouldShowLoginNudgeUseCase = useCase(
        preferences = FakeLoginNudgePreferences(
            snoozedAt = snoozedAt,
            dontShowAgain = dontShowAgain,
            firstActionAt = firstActionAt,
        ),
        userId = userId,
        isOnline = isOnline,
    )

    private fun useCase(
        preferences: FakeLoginNudgePreferences,
        userId: String? = null,
        isOnline: Boolean = true,
    ): ShouldShowLoginNudgeUseCase = ShouldShowLoginNudgeUseCase(
        getAuthenticatedUserId = { userId },
        isConnected = { isOnline },
        loginNudgePreferences = preferences,
        currentTimestampProvider = { NOW },
    )

    private companion object {
        const val NOW = 1_000_000_000L
        const val SNOOZE_MILLIS = 24L * 60 * 60 * 1000
        const val GRACE_MILLIS = 12L * 60 * 60 * 1000
    }
}
