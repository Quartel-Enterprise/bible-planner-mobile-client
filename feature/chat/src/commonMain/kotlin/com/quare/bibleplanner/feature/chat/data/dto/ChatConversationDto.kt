package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatConversationDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String?,
    @SerialName("preview") val preview: String?,
    @SerialName("context_type") val contextType: String?,
    @SerialName("context") val context: ChatContextDto?,
    @SerialName("updated_at") val updatedAt: String,
)
