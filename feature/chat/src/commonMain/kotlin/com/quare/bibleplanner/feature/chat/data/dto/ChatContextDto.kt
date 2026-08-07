package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The conversation context as the server froze it. Only [label] is rendered by the client (the
 * context pill and the input placeholder); the rest is the server's own bookkeeping.
 */
@Serializable
internal data class ChatContextDto(
    @SerialName("label") val label: String,
)
