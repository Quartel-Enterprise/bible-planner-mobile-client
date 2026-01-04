package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage

fun interface GetOfferingsResultUseCase {
    suspend operator fun invoke(): Result<List<StorePackage>>
}
