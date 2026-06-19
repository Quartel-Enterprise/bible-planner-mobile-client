package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.collectLatest

internal class SyncBillingUserIdUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val billingUserAccount: BillingUserAccount,
) : SyncBillingUserId {
    override suspend fun invoke() {
        observeAuthenticatedUserId().collectLatest { userId ->
            if (userId != null) {
                billingUserAccount.logIn(userId)
            } else {
                billingUserAccount.logOut()
            }
        }
    }
}
