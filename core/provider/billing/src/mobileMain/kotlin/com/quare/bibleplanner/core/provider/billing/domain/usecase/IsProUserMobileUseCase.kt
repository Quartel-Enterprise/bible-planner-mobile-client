package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal class IsProUserMobileUseCase(
    private val isProVerificationRequired: IsProVerificationRequiredUseCase,
    private val isProUserInRevenueCat: IsProUserInRevenueCatUseCase,
) : IsProUserUseCase {
    override suspend fun invoke(): Boolean = isProUserInRevenueCat() || !isProVerificationRequired()
}
