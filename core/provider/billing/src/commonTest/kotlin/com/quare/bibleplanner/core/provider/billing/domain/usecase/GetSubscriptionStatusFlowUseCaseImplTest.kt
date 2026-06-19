package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSubscriptionStatusFlowUseCaseImplTest {
    @Test
    fun `GIVEN verification not required WHEN observing THEN emits enterprise pro`() = runTest {
        // Given
        val useCase = createUseCase(
            isVerificationRequired = false,
            storeStatus = SubscriptionStatus.Free,
        )

        // When
        val result = useCase().first()

        // Then
        assertEquals(
            SubscriptionStatus.Pro(
                planName = "Enterprise Plan",
                purchaseDate = null,
                expirationDate = null,
                willRenew = false,
            ),
            result,
        )
    }

    @Test
    fun `GIVEN verification required WHEN observing THEN emits store status`() = runTest {
        // Given
        val useCase = createUseCase(
            isVerificationRequired = true,
            storeStatus = SubscriptionStatus.Free,
        )

        // When
        val result = useCase().first()

        // Then
        assertEquals(SubscriptionStatus.Free, result)
    }

    private fun createUseCase(
        isVerificationRequired: Boolean,
        storeStatus: SubscriptionStatus,
    ): GetSubscriptionStatusFlowUseCaseImpl = GetSubscriptionStatusFlowUseCaseImpl(
        isProVerificationRequired = ObserveProVerificationRequired { flowOf(isVerificationRequired) },
        observeStoreSubscriptionStatus = ObserveStoreSubscriptionStatus { flowOf(storeStatus) },
    )
}
