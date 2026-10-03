package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.devices.domain.usecase.UnregisterCurrentDevice
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

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
