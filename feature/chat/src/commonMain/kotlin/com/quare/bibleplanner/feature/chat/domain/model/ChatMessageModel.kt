package com.quare.bibleplanner.feature.chat.domain.model

import kotlin.time.Instant

data class ChatMessageModel(
    val id: String,
    val role: ChatRoleModel,
    val content: String,
    val isStreaming: Boolean,
    val isFailed: Boolean,
    val createdAt: Instant,
)
