package com.quare.bibleplanner.core.user.domain.usecase

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.user.domain.model.UserModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ObserveCurrentUserUseCase(
    private val sessionStatus: StateFlow<SessionStatus>,
    private val sessionUserMapper: SessionUserMapper,
) : ObserveCurrentUser {
    override fun invoke(): Flow<UserModel?> = sessionStatus
        .map { status -> (status as? SessionStatus.Authenticated)?.session?.user?.let(sessionUserMapper::map) }
        .distinctUntilChanged()
}
