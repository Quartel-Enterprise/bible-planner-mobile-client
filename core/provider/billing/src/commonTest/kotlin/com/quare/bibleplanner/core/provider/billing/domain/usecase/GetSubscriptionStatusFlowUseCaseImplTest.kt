package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSubscriptionStatusFlowUseCaseImplTest {
    @Test
    fun `GIVEN a store status WHEN observing THEN emits the store status`() = runTest {
        // Given
        val useCase = createUseCase(storeStatus = SubscriptionStatus.Free)

        // When
        val result = useCase().first()

        // Then
        assertEquals(SubscriptionStatus.Free, result)
    }

    private fun createUseCase(storeStatus: SubscriptionStatus): GetSubscriptionStatusFlowUseCaseImpl =
        GetSubscriptionStatusFlowUseCaseImpl(
            observeStoreSubscriptionStatus = ObserveStoreSubscriptionStatus { flowOf(storeStatus) },
        )
}
