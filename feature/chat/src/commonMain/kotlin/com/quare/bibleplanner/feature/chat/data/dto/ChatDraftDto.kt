package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatDraftDto(
    @SerialName("user_id") val userId: String,
    @SerialName("thread_key") val threadKey: String,
    @SerialName("content") val content: String,
    @SerialName("updated_at") val updatedAt: String,
)
