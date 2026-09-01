package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AskChatRequestDto(
    @SerialName("conversation_id") val conversationId: String?,
    @SerialName("message") val message: String,
    @SerialName("language") val language: String,
    @SerialName("context") val context: ChatContextRequestDto?,
)
