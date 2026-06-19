package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IsProUserUseCaseImplTest {
    @Test
    fun `GIVEN pro in RevenueCat WHEN checking THEN is pro`() = runTest {
        // Given
        val useCase = createUseCase(isProInRevenueCat = true, isVerificationRequired = true)

        // When
        val result = useCase()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `GIVEN not pro in RevenueCat but verification not required WHEN checking THEN is pro`() = runTest {
        // Given
        val useCase = createUseCase(isProInRevenueCat = false, isVerificationRequired = false)

        // When
        val result = useCase()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `GIVEN not pro in RevenueCat and verification required WHEN checking THEN is not pro`() = runTest {
        // Given
        val useCase = createUseCase(isProInRevenueCat = false, isVerificationRequired = true)

        // When
        val result = useCase()

        // Then
        assertEquals(false, result)
    }

    private fun createUseCase(
        isProInRevenueCat: Boolean,
        isVerificationRequired: Boolean,
    ): IsProUserUseCaseImpl = IsProUserUseCaseImpl(
        isProVerificationRequired = IsProVerificationRequired { isVerificationRequired },
        isProUserInRevenueCat = IsProUserInRevenueCat { isProInRevenueCat },
    )
}
