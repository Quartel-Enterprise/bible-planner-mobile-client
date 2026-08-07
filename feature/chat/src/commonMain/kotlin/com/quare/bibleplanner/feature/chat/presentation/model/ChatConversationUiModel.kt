package com.quare.bibleplanner.feature.chat.presentation.model

import com.quare.bibleplanner.core.date.RelativeTime

data class ChatConversationUiModel(
    val id: String,
    val title: String,
    val preview: String?,
    val relativeTime: RelativeTime,
    val isActive: Boolean,
)
