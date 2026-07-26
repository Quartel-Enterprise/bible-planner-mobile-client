package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OfferingsResponseDto(
    @SerialName("current_offering_id")
    val currentOfferingId: String?,
    @SerialName("offerings")
    val offerings: List<OfferingDto>,
)
