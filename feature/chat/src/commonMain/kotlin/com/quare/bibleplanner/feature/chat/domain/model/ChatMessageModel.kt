package com.quare.bibleplanner.feature.chat.domain.model

import kotlin.time.Instant

/**
 * One message of a conversation, keyed by the id the server assigned it. [isStreaming] marks the
 * answer currently being written token by token: its [content] grows until the stream completes.
 * [isFailed] marks an answer whose generation did not finish — the server records it so every
 * device shows the same thread, rather than a question that may or may not still be answered.
 */
data class ChatMessageModel(
    val id: String,
    val role: ChatRoleModel,
    val content: String,
    val isStreaming: Boolean,
    val isFailed: Boolean,
    val createdAt: Instant,
)
