package com.quare.bibleplanner.feature.chat.domain.coordinator

// Why: owned for the app's lifetime; per screen, the realtime client reuses the channel for
// a topic, so one visit's teardown unsubscribed the channel the next visit had claimed.
fun interface ChatSyncCoordinator {
    fun ensureStarted()
}
