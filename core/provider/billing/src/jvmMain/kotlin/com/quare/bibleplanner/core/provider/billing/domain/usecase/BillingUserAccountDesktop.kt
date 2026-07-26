package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository

internal class BillingUserAccountDesktop(
    private val repository: DesktopBillingRepository,
) : BillingUserAccount {
    override suspend fun logIn(userId: String) {
        repository.refreshSubscriptionStatus()
    }

    override suspend fun logOut() {
        repository.clearSubscriptionStatus()
    }
}
