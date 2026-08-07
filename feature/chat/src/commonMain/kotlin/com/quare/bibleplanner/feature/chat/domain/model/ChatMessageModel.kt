package com.quare.bibleplanner.feature.chat.domain.model

import kotlin.time.Instant

/**
 * One message of a conversation, keyed by the id the server assigned it. [isStreaming] marks the
 * answer currently being written token by token: its [content] grows until the stream completes.
 */
data class ChatMessageModel(
    val id: String,
    val role: ChatRoleModel,
    val content: String,
    val isStreaming: Boolean,
    val createdAt: Instant,
)
