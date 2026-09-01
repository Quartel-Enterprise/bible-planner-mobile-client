package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatAcceptedDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_message_id") val userMessageId: String,
    @SerialName("assistant_message_id") val assistantMessageId: String,
    @SerialName("is_new_conversation") val isNewConversation: Boolean,
    @SerialName("context_label") val contextLabel: String?,
    @SerialName("title") val title: String,
)
