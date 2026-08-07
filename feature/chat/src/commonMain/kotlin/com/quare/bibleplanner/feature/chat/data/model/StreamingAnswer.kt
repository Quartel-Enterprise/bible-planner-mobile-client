package com.quare.bibleplanner.feature.chat.data.model

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.time.Instant

/**
 * The answer being written right now. It is deliberately not cached: until the stream completes,
 * this text is a partial view of something the server has not finished (and may restart), so it
 * lives here and is combined with the cached thread only for display.
 */
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
        createdAt = createdAt,
    )
}
