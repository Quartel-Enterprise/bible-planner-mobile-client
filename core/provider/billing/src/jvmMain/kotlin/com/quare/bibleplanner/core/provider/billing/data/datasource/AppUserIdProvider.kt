package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId

internal class AppUserIdProvider(
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val getAnonymousAppUserId: GetAnonymousAppUserId,
) : GetAppUserId {
    override suspend fun invoke(): String = getAuthenticatedUserId() ?: getAnonymousAppUserId()
}
