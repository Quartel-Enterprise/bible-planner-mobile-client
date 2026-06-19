package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsProVerificationRequired {
    suspend operator fun invoke(): Boolean
}
