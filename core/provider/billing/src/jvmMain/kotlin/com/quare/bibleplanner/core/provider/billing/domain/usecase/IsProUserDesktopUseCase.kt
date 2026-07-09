package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal class IsProUserDesktopUseCase : IsProUserUseCase {
    override suspend fun invoke(): Boolean = true
}
