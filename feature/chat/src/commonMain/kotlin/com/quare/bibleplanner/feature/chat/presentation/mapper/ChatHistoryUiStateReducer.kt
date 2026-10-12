package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.feature.chat.presentation.model.ChatHistoryUiState

internal fun ChatHistoryUiState.toggleActionsOf(conversationId: String): ChatHistoryUiState = copy(
    expandedActionsId = conversationId.takeIf { it != expandedActionsId },
    renamingId = null,
    deletingId = null,
)

internal fun ChatHistoryUiState.startRename(
    conversationId: String,
    title: String,
): ChatHistoryUiState = copy(
    renamingId = conversationId,
    renameDraft = title,
    deletingId = null,
)

internal fun ChatHistoryUiState.cancelRename(): ChatHistoryUiState = copy(
    renamingId = null,
    renameDraft = "",
)

internal fun ChatHistoryUiState.closeRename(): ChatHistoryUiState = copy(
    renamingId = null,
    renameDraft = "",
    expandedActionsId = null,
)

internal fun ChatHistoryUiState.closeDelete(): ChatHistoryUiState = copy(
    deletingId = null,
    expandedActionsId = null,
)
