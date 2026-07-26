package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal class IsFreeUserUseCaseImpl(
    private val isProUser: IsProUserUseCase,
) : IsFreeUserUseCase {
    override suspend fun invoke(): Boolean = !isProUser()
}
