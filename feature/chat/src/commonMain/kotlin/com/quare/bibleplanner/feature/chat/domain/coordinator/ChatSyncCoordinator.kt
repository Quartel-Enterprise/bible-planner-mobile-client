package com.quare.bibleplanner.feature.chat.domain.coordinator

/**
 * Owns the chat's realtime subscription for the life of the app. Per screen it could not: the
 * realtime client returns the same channel instance for the same topic, so one visit's teardown
 * unsubscribed the channel the next visit had just claimed.
 */
fun interface ChatSyncCoordinator {
    fun ensureStarted()
}
