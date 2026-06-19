package com.quare.bibleplanner.core.provider.billing.domain.usecase

interface BillingUserAccount {
    suspend fun logIn(userId: String)

    suspend fun logOut()
}
