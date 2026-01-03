package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.mapper.PackageMapper
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.result.awaitOfferingsResult
import kotlin.collections.orEmpty
import kotlin.map

internal class GetOfferingsResultMobileUseCase(
    private val packageMapper: PackageMapper,
) : GetOfferingsResultUseCase {
    override suspend fun invoke(): Result<List<StorePackage>> = Purchases.sharedInstance
        .awaitOfferingsResult()
        .map { offerings: Offerings ->
            val current = offerings.current
            current?.availablePackages?.map(packageMapper::toStorePackage).orEmpty()
        }
}
