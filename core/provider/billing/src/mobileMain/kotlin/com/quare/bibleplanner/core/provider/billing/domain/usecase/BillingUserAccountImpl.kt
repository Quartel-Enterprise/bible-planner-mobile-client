package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.result.awaitLogInResult
import com.revenuecat.purchases.kmp.result.awaitLogOutResult

internal class BillingUserAccountImpl(
    private val purchases: Purchases,
) : BillingUserAccount {
    override suspend fun logIn(userId: String) {
        purchases.awaitLogInResult(userId)
    }

    override suspend fun logOut() {
        purchases.awaitLogOutResult()
    }
}
