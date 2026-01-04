package com.quare.bibleplanner.core.provider.billing.domain.usecase

class IsFreeUserMobileUseCase(
    private val isPremiumUser: IsPremiumUserUseCase,
) : IsFreeUserUseCase {
    override suspend fun invoke(): Boolean = !isPremiumUser()
}
