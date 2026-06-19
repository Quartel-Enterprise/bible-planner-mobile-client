package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface SyncBillingUserId {
    suspend operator fun invoke()
}
