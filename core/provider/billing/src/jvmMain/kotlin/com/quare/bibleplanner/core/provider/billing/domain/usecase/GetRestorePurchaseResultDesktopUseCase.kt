package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class GetRestorePurchaseResultDesktopUseCase(
    private val repository: DesktopBillingRepository,
) : GetRestorePurchaseResultUseCase {
    override suspend fun invoke(): Result<Unit> = suspendRunCatching {
        if (repository.refreshSubscriptionStatus() !is SubscriptionStatus.Pro) {
            throw BillingException.RestorePurchaseFailed()
        }
    }
}
