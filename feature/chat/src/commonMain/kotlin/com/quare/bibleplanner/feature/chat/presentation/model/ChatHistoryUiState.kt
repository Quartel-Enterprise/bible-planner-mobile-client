package com.quare.bibleplanner.feature.chat.presentation.model

data class ChatHistoryUiState(
    val isOpen: Boolean,
    val query: String,
    val groups: List<ChatConversationGroupUiModel>,
    val hasConversations: Boolean,
    val expandedActionsId: String?,
    val renamingId: String?,
    val renameDraft: String,
    val deletingId: String?,
)
