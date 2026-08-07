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

/**
 * The thread as the reader should see it: the cached messages, with the answer being written now
 * standing in for its own cached row.
 *
 * That row can already be there — the server inserts the answer before the stream reports `done`,
 * and Realtime often delivers it first — and appending the live one on top would put the same id in
 * the list twice, which a lazy list refuses outright.
 */
internal fun List<ChatMessageModel>.withStreamingAnswer(
    streaming: StreamingAnswer?,
    conversationId: String,
): List<ChatMessageModel> {
    if (streaming == null || streaming.conversationId != conversationId) return this
    return filterNot { message -> message.id == streaming.messageId } + streaming.toMessage()
}
