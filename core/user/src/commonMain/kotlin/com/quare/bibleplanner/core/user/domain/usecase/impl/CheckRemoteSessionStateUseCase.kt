package com.quare.bibleplanner.core.user.domain.usecase.impl

import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState
import com.quare.bibleplanner.core.user.domain.usecase.CheckRemoteSessionState
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.exceptions.RestException

internal class CheckRemoteSessionStateUseCase(
    private val auth: Auth,
) : CheckRemoteSessionState {
    private val clientErrorStatusCodes = 400..499

    override suspend fun invoke(): RemoteSessionState = suspendRunCatching { auth.refreshCurrentSession() }
        .fold(
            onSuccess = { RemoteSessionState.ACTIVE },
            onFailure = { it.toRemoteSessionState() },
        )

    // Why: a rate-limited refresh says nothing about the session, so only other client errors mean it was revoked.
    private fun Throwable.toRemoteSessionState(): RemoteSessionState =
        if (this is RestException && statusCode in clientErrorStatusCodes && statusCode != TOO_MANY_REQUESTS_STATUS) {
            RemoteSessionState.REVOKED
        } else {
            RemoteSessionState.UNKNOWN
        }

    private companion object {
        const val TOO_MANY_REQUESTS_STATUS = 429
    }
}
