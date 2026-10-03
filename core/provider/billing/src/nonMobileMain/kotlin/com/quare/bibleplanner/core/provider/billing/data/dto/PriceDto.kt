package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PriceDto(
    @SerialName("amount_micros")
    val amountMicros: Long,
    @SerialName("currency")
    val currency: String,
)
