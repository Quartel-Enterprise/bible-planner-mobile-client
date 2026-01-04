package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsPremiumUserUseCase {
    suspend operator fun invoke(): Boolean
}
