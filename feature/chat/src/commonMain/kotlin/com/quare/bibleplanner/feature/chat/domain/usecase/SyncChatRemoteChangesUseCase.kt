package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class SyncChatRemoteChangesUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke() {
        repository.syncRemoteChanges()
    }
}
