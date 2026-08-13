package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class SaveChatDraftUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(
        threadKey: String,
        content: String,
    ) {
        repository.saveDraft(
            threadKey = threadKey,
            content = content,
        )
    }
}
