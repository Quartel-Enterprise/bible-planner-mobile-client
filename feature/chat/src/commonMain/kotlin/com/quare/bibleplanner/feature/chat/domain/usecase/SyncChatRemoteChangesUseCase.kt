package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.coordinator.ChatSyncCoordinator

class SyncChatRemoteChangesUseCase(
    private val coordinator: ChatSyncCoordinator,
) {
    operator fun invoke() {
        coordinator.ensureStarted()
    }
}
