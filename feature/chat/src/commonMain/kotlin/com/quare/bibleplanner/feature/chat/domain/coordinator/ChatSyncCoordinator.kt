package com.quare.bibleplanner.feature.chat.domain.coordinator

/**
 * Owns the chat's realtime subscription for the life of the app, like the sync engine owns every
 * other dataset's.
 *
 * It used to live in each chat screen's own scope, and the teardown of one visit could kill the
 * next: the realtime client hands back the same channel instance for the same topic, so the
 * previous screen's late removal unsubscribed the channel the new screen had just claimed — from
 * the second visit on, live updates arrived only by luck. Started once and never stopped, there is
 * nothing left to race.
 */
fun interface ChatSyncCoordinator {
    fun ensureStarted()
}
