package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProductDto(
    @SerialName("identifier")
    val identifier: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String?,
    @SerialName("current_price")
    val currentPrice: PriceDto,
)
