package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatDoneDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("assistant_message_id") val assistantMessageId: String,
    @SerialName("content") val content: String,
    @SerialName("used_count") val usedCount: Int,
    @SerialName("free_limit") val freeLimit: Int,
    @SerialName("is_pro") val isPro: Boolean,
)
