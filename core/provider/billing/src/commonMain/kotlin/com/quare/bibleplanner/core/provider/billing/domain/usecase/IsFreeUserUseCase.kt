package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsFreeUserUseCase {
    suspend operator fun invoke(): Boolean
}
