package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProductsResponseDto(
    @SerialName("product_details")
    val productDetails: List<ProductDto>,
)
