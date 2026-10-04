package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccount

internal class SignedOutBillingUserAccount : BillingUserAccount {
    override suspend fun logIn(userId: String) {}

    override suspend fun logOut() {}

    override fun setFirebaseAppInstanceId(firebaseAppInstanceId: String?) {}
}
