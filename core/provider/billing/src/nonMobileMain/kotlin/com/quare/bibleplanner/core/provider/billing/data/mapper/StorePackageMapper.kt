package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PackageDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductDto
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType

internal class StorePackageMapper(
    private val priceFormatter: PriceFormatter,
) {
    fun map(
        packageDto: PackageDto,
        product: ProductDto,
    ): StorePackage = StorePackage(
        identifier = packageDto.identifier,
        priceString = priceFormatter.format(product.currentPrice),
        priceMicros = product.currentPrice.amountMicros,
        title = product.title,
        description = product.description.orEmpty(),
        type = packageDto.identifier.toStorePackageType(),
    )

    private fun String.toStorePackageType(): StorePackageType = when (this) {
        MONTHLY_PACKAGE_IDENTIFIER -> StorePackageType.MONTHLY
        ANNUAL_PACKAGE_IDENTIFIER -> StorePackageType.ANNUAL
        else -> StorePackageType.UNKNOWN
    }

    private companion object {
        const val MONTHLY_PACKAGE_IDENTIFIER = $$"$rc_monthly"
        const val ANNUAL_PACKAGE_IDENTIFIER = $$"$rc_annual"
    }
}
