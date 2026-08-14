package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class SaveChatDraftUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(draft: PendingDraftModel) {
        repository.saveDraft(draft)
    }
}
