package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsFreeUserUseCaseImplTest {
    @Test
    fun `GIVEN a pro user WHEN checking THEN is not free`() = runTest {
        // Given
        val useCase = IsFreeUserUseCaseImpl { true }

        // When
        val isFree = useCase()

        // Then
        assertFalse(isFree)
    }

    @Test
    fun `GIVEN a user without pro WHEN checking THEN is free`() = runTest {
        // Given
        val useCase = IsFreeUserUseCaseImpl { false }

        // When
        val isFree = useCase()

        // Then
        assertTrue(isFree)
    }
}
