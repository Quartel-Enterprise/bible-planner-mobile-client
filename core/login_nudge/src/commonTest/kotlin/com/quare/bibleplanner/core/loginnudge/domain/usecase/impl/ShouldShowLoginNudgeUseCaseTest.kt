package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.loginnudge.fake.FakeLoginNudgePreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

internal class ShouldShowLoginNudgeUseCaseTest {
    private val snooze: Duration = 24.hours
    private val grace: Duration = 12.hours

    @Test
    fun `GIVEN an authenticated user WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(userId = "user-1")

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN a permanently dismissed nudge WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(dontShowAgain = true)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN the device offline WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(isOnline = false)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN a nudge never snoozed WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(snoozedAt = null)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertTrue(shouldShow)
    }

    @Test
    fun `GIVEN a nudge within the snooze window WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(snoozedAt = NOW - snooze.inWholeMilliseconds + 1)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN an elapsed snooze window WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(snoozedAt = NOW - snooze.inWholeMilliseconds)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertTrue(shouldShow)
    }

    @Test
    fun `GIVEN a snooze timestamp in the future WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(snoozedAt = NOW + snooze.inWholeMilliseconds)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertTrue(shouldShow)
    }

    @Test
    fun `GIVEN a first action timestamp in the future WHEN evaluating THEN returns true`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(firstActionAt = NOW + grace.inWholeMilliseconds)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertTrue(shouldShow)
    }

    @Test
    fun `GIVEN a nudge within the first action grace period WHEN evaluating THEN returns false`() = runTest {
        // Given
        val shouldShowLoginNudge = useCase(firstActionAt = NOW - grace.inWholeMilliseconds + 1)

        // When
        val shouldShow = shouldShowLoginNudge()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN no first action yet WHEN evaluating THEN stamps the first action and returns false`() = runTest {
        // Given
        val preferences = FakeLoginNudgePreferences(
            snoozedAt = null,
            dontShowAgain = false,
            firstActionAt = null,
        )
        val shouldShowLoginNudge = useCase(preferences)

        // When
        val result = shouldShowLoginNudge()

        // Then
        assertFalse(result)
        assertEquals(NOW, preferences.getFirstActionAt())
    }

    private fun useCase(
        userId: String? = null,
        isOnline: Boolean = true,
        snoozedAt: Long? = null,
        dontShowAgain: Boolean = false,
        firstActionAt: Long? = NOW - grace.inWholeMilliseconds,
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
        hasCooldownElapsed = HasCooldownElapsedUseCase { NOW },
    )

    private companion object {
        const val NOW = 1_000_000_000L
    }
}
