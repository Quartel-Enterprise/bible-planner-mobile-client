package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

internal class GetSubscriptionStatusFlowUseCaseImpl(
    private val observeStoreSubscriptionStatus: ObserveStoreSubscriptionStatus,
) : GetSubscriptionStatusFlowUseCase {
    override fun invoke(): Flow<SubscriptionStatus> = observeStoreSubscriptionStatus()
}
