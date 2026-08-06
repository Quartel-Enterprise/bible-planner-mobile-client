package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatTitleDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("title") val title: String,
)
