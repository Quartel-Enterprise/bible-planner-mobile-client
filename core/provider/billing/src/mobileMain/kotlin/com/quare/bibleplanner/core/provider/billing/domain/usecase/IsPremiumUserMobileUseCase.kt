package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal class IsPremiumUserMobileUseCase(
    private val isPremiumVerificationRequired: IsPremiumVerificationRequiredUseCase,
    private val isProUserInRevenueCat: IsProUserInRevenueCatUseCase,
) : IsPremiumUserUseCase {
    override suspend fun invoke(): Boolean = isProUserInRevenueCat() || !isPremiumVerificationRequired()
}
