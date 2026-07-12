package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.devices.domain.usecase.UnregisterCurrentDevice
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

/**
 * Removes this device's connected-devices row (best-effort, while still authenticated, so other
 * devices see it leave the list live over realtime; the server prunes stale rows as a fallback),
 * then leaves realtime channels (while still authenticated, so the unsubscribe phx_leave isn't sent
 * during auth teardown) and disconnects the socket, so it doesn't keep reconnecting on the shared
 * HTTP engine and stall the next login's auth request with a stale pooled connection. Then
 * [Auth.signOut] — also stops the favorites sync pipeline, so it won't re-pull remote data into the
 * database after we clear it — and clears local user data only after sign-out succeeded; if sign-out
 * fails (e.g. offline) we keep the data and stay logged in.
 */
class EndSessionUseCase(
    private val auth: Auth,
    private val realtime: Realtime,
    private val clearLocalUserData: ClearLocalUserData,
    private val unregisterCurrentDevice: UnregisterCurrentDevice,
) : EndSession {
    override suspend fun invoke(): Result<Unit> {
        unregisterCurrentDevice()
        return suspendRunCatching {
            realtime.removeAllChannels()
        }.map {
            realtime.disconnect()
            suspendRunCatching {
                auth.signOut()
                clearLocalUserData()
            }
        }
    }
}
