package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SubscriberResponseDto(
    @SerialName("request_date_ms")
    val requestDateMillis: Long,
    @SerialName("subscriber")
    val subscriber: SubscriberDto,
)
