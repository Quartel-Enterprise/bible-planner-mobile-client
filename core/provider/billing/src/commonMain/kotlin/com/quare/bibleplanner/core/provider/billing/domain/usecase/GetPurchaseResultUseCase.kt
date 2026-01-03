package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage

fun interface GetPurchaseResultUseCase {
    suspend operator fun invoke(storePackage: StorePackage): Result<Unit>
}
