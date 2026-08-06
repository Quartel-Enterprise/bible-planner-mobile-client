package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatConversationTitleUpdateDto(
    @SerialName("title") val title: String,
)
