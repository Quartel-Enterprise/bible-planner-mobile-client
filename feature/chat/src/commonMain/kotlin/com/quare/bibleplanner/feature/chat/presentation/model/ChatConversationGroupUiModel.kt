package com.quare.bibleplanner.feature.chat.presentation.model

data class ChatConversationGroupUiModel(
    val bucket: ChatConversationBucket,
    val conversations: List<ChatConversationUiModel>,
)
