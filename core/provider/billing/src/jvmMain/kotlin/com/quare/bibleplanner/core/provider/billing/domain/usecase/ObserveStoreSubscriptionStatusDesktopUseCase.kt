package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class ObserveStoreSubscriptionStatusDesktopUseCase(
    private val repository: DesktopBillingRepository,
) : ObserveStoreSubscriptionStatus {
    override fun invoke(): Flow<SubscriptionStatus> = repository.subscriptionStatus
        .onStart { repository.refreshSubscriptionStatus() }
        .map { status -> status ?: SubscriptionStatus.Free }
}
