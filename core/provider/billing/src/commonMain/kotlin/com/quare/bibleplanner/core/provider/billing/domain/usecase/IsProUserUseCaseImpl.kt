package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal class IsProUserUseCaseImpl(
    private val isProVerificationRequired: IsProVerificationRequired,
    private val isProUserInRevenueCat: IsProUserInRevenueCat,
) : IsProUserUseCase {
    override suspend fun invoke(): Boolean = isProUserInRevenueCat() || !isProVerificationRequired()
}
