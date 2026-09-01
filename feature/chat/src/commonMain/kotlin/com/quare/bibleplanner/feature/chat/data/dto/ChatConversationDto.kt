package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class ChatConversationDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String?,
    @SerialName("preview") val preview: String?,
    @SerialName("context_type") val contextType: String?,
    // Raw json, not a typed shape: kotlinx only treats a missing key as null when the property has
    // a default, so naming a field the server added later would fail every older row.
    @SerialName("context") val context: JsonObject?,
    @SerialName("updated_at") val updatedAt: String,
)
