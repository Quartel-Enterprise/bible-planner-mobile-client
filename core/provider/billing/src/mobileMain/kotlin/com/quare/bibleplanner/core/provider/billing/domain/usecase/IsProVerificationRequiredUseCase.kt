package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.flow.first

internal class IsProVerificationRequiredUseCase(
    private val observeProVerificationRequired: ObserveProVerificationRequired,
) : IsProVerificationRequired {
    override suspend fun invoke(): Boolean = observeProVerificationRequired().first()
}
