package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PackageDto(
    @SerialName("identifier")
    val identifier: String,
    @SerialName("platform_product_identifier")
    val platformProductIdentifier: String,
)
