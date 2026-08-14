package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class LoadChatMessagesUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> = suspendRunCatching {
        repository.refreshMessages(conversationId)
    }
}
