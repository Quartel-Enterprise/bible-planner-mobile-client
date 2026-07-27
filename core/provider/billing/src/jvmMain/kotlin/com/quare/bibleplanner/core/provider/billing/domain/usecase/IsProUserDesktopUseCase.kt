package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository

internal class IsProUserDesktopUseCase(
    private val repository: DesktopBillingRepository,
) : IsProUserUseCase {
    override suspend fun invoke(): Boolean {
        val status = repository.subscriptionStatus.value ?: repository.refreshSubscriptionStatus()
        return status is SubscriptionStatus.Pro
    }
}
