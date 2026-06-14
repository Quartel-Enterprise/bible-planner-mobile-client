package com.quare.bibleplanner.core.user.domain.usecase

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ObserveAuthenticatedUserIdUseCase(
    private val sessionStatus: StateFlow<SessionStatus>,
) : ObserveAuthenticatedUserId {
    override fun invoke(): Flow<String?> = sessionStatus
        .map { status -> (status as? SessionStatus.Authenticated)?.session?.user?.id }
        .distinctUntilChanged()
}
