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
    // Read as raw json rather than a typed shape: the server grows this payload over time, and a
    // field a conversation was frozen before simply is not there. Declared, it would be required —
    // kotlinx only treats a missing key as null when the property has a default — and one older
    // conversation would fail the whole list.
    @SerialName("context") val context: JsonObject?,
    @SerialName("updated_at") val updatedAt: String,
)
