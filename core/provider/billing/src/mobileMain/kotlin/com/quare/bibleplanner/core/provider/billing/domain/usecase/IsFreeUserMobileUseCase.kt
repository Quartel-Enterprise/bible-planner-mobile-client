package com.quare.bibleplanner.core.provider.billing.domain.usecase

class IsFreeUserMobileUseCase(
    private val isProUser: IsProUserUseCase,
) : IsFreeUserUseCase {
    override suspend fun invoke(): Boolean = !isProUser()
}
