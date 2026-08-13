package com.quare.bibleplanner.feature.chat.data.model

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.time.Instant

internal data class StreamingAnswer(
    val conversationId: String,
    val messageId: String,
    val content: String,
    val createdAt: Instant,
) {
    fun toMessage(): ChatMessageModel = ChatMessageModel(
        id = messageId,
        role = ChatRoleModel.ASSISTANT,
        content = content,
        isStreaming = true,
        isFailed = false,
        createdAt = createdAt,
    )
}

internal fun List<ChatMessageModel>.withStreamingAnswer(
    streaming: StreamingAnswer?,
    conversationId: String,
): List<ChatMessageModel> {
    if (streaming == null || streaming.conversationId != conversationId) return this
    return filterNot { message -> message.id == streaming.messageId } + streaming.toMessage()
}
