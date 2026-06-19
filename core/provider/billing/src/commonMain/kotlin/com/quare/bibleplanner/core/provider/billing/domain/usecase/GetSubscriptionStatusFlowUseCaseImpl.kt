package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class GetSubscriptionStatusFlowUseCaseImpl(
    private val isProVerificationRequired: ObserveProVerificationRequired,
    private val observeStoreSubscriptionStatus: ObserveStoreSubscriptionStatus,
) : GetSubscriptionStatusFlowUseCase {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<SubscriptionStatus> = isProVerificationRequired().flatMapLatest { isRequired ->
        if (isRequired) {
            observeStoreSubscriptionStatus()
        } else {
            flowOf(enterpriseSubscriptionStatus())
        }
    }

    private fun enterpriseSubscriptionStatus(): SubscriptionStatus = SubscriptionStatus.Pro(
        planName = "Enterprise Plan",
        purchaseDate = null,
        expirationDate = null,
        willRenew = false,
    )
}
