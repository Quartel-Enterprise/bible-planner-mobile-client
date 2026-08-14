package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatMessagesUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(conversationId: String): Flow<List<ChatMessageModel>> =
        repository.observeMessages(conversationId)
}
