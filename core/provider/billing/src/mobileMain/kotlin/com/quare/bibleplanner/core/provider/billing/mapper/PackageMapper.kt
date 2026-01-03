package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType

internal class PackageMapper {
    fun toStorePackage(packageModel: Package): StorePackage = packageModel.run {
        StorePackage(
            identifier = identifier,
            priceString = storeProduct.price.formatted,
            priceMicros = storeProduct.price.amountMicros,
            title = storeProduct.title,
            description = "",
            type = when (packageType) {
                PackageType.MONTHLY -> StorePackageType.MONTHLY
                PackageType.ANNUAL -> StorePackageType.ANNUAL
                else -> StorePackageType.UNKNOWN
            },
            originalObject = this,
        )
    }
}
