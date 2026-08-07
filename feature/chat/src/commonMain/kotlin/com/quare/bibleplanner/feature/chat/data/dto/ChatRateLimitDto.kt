package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatRateLimitDto(
    @SerialName("retry_after_seconds") val retryAfterSeconds: Int,
)
