package com.quare.bibleplanner.core.date

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

internal class HasCooldownElapsedUseCaseTest {
    private lateinit var useCase: HasCooldownElapsedUseCase

    @BeforeTest
    fun setUp() {
        useCase = HasCooldownElapsedUseCase { NOW }
    }

    @Test
    fun `GIVEN no previous occurrence WHEN checking the cooldown THEN has elapsed`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = null,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `GIVEN a cooldown that has passed WHEN checking the cooldown THEN has elapsed`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = NOW - 4.hours.inWholeMilliseconds,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `GIVEN a last occurrence in the future WHEN checking the cooldown THEN has elapsed`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = NOW + 26.hours.inWholeMilliseconds,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `GIVEN a last occurrence within the cooldown window WHEN checking the cooldown THEN has not elapsed`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = NOW - 4.hours.inWholeMilliseconds + 1,
            cooldown = 4.hours,
        )

        // Then
        assertFalse(hasElapsed)
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
