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
    fun `elapses when there is no previous occurrence`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = null,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `elapses when the cooldown has passed`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = NOW - 4.hours.inWholeMilliseconds,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `elapses when the last occurrence is in the future`() {
        // When
        val hasElapsed = useCase(
            lastOccurredAt = NOW + 26.hours.inWholeMilliseconds,
            cooldown = 4.hours,
        )

        // Then
        assertTrue(hasElapsed)
    }

    @Test
    fun `does not elapse within the cooldown window`() {
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
