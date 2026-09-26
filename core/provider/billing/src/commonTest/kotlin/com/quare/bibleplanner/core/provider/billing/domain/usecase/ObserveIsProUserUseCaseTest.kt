package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveIsProUserUseCaseTest {
    private val pro = SubscriptionStatus.Pro(
        planType = ProPlanType.MONTHLY,
        purchaseDate = null,
        expirationDate = null,
        willRenew = true,
        store = null,
    )

    @Test
    fun `GIVEN status changes WHEN observing THEN emits whether the user is pro only when it changes`() = runTest {
        // Given
        val useCase =
            ObserveIsProUserUseCase { flowOf(null, SubscriptionStatus.Free, pro, pro, SubscriptionStatus.Free) }

        // When
        val emissions = useCase().toList()

        // Then
        assertEquals(
            expected = listOf(false, true, false),
            actual = emissions,
        )
    }
}
