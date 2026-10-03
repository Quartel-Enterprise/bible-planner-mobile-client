package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EntitlementDto(
    @SerialName("product_identifier")
    val productIdentifier: String,
    @SerialName("purchase_date")
    val purchaseDate: String?,
    @SerialName("expires_date")
    val expiresDate: String?,
    @SerialName("grace_period_expires_date")
    val gracePeriodExpiresDate: String?,
)
