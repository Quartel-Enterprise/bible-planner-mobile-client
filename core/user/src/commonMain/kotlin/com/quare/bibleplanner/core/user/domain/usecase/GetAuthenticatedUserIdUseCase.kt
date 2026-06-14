package com.quare.bibleplanner.core.user.domain.usecase

import kotlinx.coroutines.flow.first

internal class GetAuthenticatedUserIdUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
) : GetAuthenticatedUserId {
    override suspend fun invoke(): String? = observeAuthenticatedUserId().first()
}
