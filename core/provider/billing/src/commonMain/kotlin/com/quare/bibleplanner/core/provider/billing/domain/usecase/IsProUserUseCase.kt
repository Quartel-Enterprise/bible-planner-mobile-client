package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsProUserUseCase {
    suspend operator fun invoke(): Boolean
}
