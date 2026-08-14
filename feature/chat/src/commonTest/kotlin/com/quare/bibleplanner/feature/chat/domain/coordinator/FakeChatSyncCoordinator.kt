package com.quare.bibleplanner.feature.chat.domain.coordinator

internal class FakeChatSyncCoordinator : ChatSyncCoordinator {
    var startedCount: Int = 0

    override fun ensureStarted() {
        startedCount++
    }
}
