package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OfferingDto(
    @SerialName("identifier")
    val identifier: String,
    @SerialName("packages")
    val packages: List<PackageDto>,
)
