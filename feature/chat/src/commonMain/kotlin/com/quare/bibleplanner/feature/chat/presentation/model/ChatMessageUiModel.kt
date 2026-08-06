package com.quare.bibleplanner.feature.chat.presentation.model

data class ChatMessageUiModel(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val isStreaming: Boolean,
)
