package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatDraftUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(threadKey: String): Flow<String> = repository.observeDraft(threadKey)
}
