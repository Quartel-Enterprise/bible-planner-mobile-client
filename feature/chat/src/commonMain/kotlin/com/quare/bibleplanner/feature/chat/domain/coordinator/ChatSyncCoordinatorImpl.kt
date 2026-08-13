package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.launch

internal class ChatSyncCoordinatorImpl(
    private val applicationScope: ApplicationScope,
    private val repository: ChatRepository,
) : ChatSyncCoordinator {
    private var isStarted = false

    override fun ensureStarted() {
        if (isStarted) return
        isStarted = true
        applicationScope.launch { repository.syncRemoteChanges() }
    }
}
