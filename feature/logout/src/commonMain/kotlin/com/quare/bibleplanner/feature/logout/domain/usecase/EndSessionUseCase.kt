package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.devices.domain.usecase.UnregisterCurrentDevice
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

/*
 * Why: unregister and leave realtime while still authenticated (others see the device leave
 * live; phx_leave isn't sent during auth teardown) and disconnect so a stale pooled socket can't
 * stall the next login. signOut stops sync re-pulls; local data is cleared only after it
 * succeeds, so an offline failure keeps the data and the session.
 */
class EndSessionUseCase(
    private val auth: Auth,
    private val realtime: Realtime,
    private val clearLocalUserData: ClearLocalUserData,
    private val unregisterCurrentDevice: UnregisterCurrentDevice,
    private val intentionalLogoutMarker: IntentionalLogoutMarker,
) : EndSession {
    override suspend fun invoke(): Result<Unit> {
        unregisterCurrentDevice()
        return suspendRunCatching {
            realtime.removeAllChannels()
        }.fold(
            onSuccess = { signOutAndClearLocalData() },
            onFailure = Result.Companion::failure,
        )
    }

    private suspend fun signOutAndClearLocalData(): Result<Unit> {
        realtime.disconnect()
        intentionalLogoutMarker.mark()
        return suspendRunCatching {
            auth.signOut()
            clearLocalUserData()
        }.onFailure {
            intentionalLogoutMarker.unmark()
        }
    }
}
