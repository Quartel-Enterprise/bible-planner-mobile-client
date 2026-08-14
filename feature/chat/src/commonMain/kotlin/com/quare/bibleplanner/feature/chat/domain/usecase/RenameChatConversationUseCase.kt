package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class RenameChatConversationUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        title: String,
    ): Result<Unit> = suspendRunCatching {
        repository.renameConversation(
            conversationId = conversationId,
            title = title.trim(),
        )
    }
}
