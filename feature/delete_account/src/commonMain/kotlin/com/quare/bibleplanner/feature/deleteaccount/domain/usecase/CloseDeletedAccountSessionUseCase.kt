package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.provider.supabase.session.ExpectedSessionDeletion
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

class CloseDeletedAccountSessionUseCase(
    private val auth: Auth,
    private val realtime: Realtime,
    private val clearLocalUserData: ClearLocalUserData,
    private val intentionalLogoutMarker: IntentionalLogoutMarker,
    private val expectedSessionDeletion: ExpectedSessionDeletion,
) : CloseDeletedAccountSession {
    override suspend fun invoke(): Result<Unit> {
        intentionalLogoutMarker.mark()
        suspendRunCatching { realtime.removeAllChannels() }
        suspendRunCatching { realtime.disconnect() }
        expectedSessionDeletion.whileDeleting {
            suspendRunCatching { auth.signOut() }
        }
        return suspendRunCatching { clearLocalUserData() }
    }
}
