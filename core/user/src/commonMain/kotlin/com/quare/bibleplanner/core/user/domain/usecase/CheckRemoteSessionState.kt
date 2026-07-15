package com.quare.bibleplanner.core.user.domain.usecase

import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState

fun interface CheckRemoteSessionState {
    suspend operator fun invoke(): RemoteSessionState
}
