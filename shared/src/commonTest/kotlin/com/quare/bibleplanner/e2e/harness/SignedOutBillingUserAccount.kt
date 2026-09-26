package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccount

internal class SignedOutBillingUserAccount : BillingUserAccount {
    override suspend fun logIn(userId: String) = Unit

    override suspend fun logOut() = Unit

    override fun setFirebaseAppInstanceId(firebaseAppInstanceId: String?) = Unit
}
