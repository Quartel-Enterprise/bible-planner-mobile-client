package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatConversationsUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(): Flow<List<ChatConversationModel>> = repository.observeConversations()
}
