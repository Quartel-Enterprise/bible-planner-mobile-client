package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ObserveIsProUserUseCase(
    private val getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase,
) : ObserveIsProUser {
    override fun invoke(): Flow<Boolean> = getSubscriptionStatusFlow()
        .map { it is SubscriptionStatus.Pro }
        .distinctUntilChanged()
}
