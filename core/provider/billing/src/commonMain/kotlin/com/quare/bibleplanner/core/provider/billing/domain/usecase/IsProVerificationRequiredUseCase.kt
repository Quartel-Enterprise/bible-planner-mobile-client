package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsProVerificationRequiredUseCase {
    suspend operator fun invoke(): Boolean
}
