package com.quare.bibleplanner.core.devices.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Emits once when THIS device's session is revoked from another device: its row disappears from the
 * synced list (via realtime) after having been present. The app reacts by ending the local session,
 * so a remote sign-out takes effect immediately while online (instead of only on the next token
 * refresh).
 */
fun interface ObserveCurrentDeviceRevoked {
    operator fun invoke(): Flow<Unit>
}
