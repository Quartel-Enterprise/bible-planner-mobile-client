package com.quare.bibleplanner.core.provider.billing.domain.model.store

data class StorePackage(
    val identifier: String,
    val priceString: String,
    val priceMicros: Long,
    val title: String,
    val description: String,
    val type: StorePackageType,
    val originalObject: Any? = null,
)
