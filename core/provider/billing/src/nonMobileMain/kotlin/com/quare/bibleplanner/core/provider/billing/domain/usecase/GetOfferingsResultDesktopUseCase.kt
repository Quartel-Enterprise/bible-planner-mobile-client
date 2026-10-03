package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class GetOfferingsResultDesktopUseCase(
    private val repository: DesktopBillingRepository,
) : GetOfferingsResultUseCase {
    override suspend fun invoke(): Result<List<StorePackage>> = suspendRunCatching {
        repository.getStorePackages()
    }
}
