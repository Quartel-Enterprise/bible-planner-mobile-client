package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface GetRestorePurchaseResultUseCase {
    suspend operator fun invoke(): Result<Unit>
}
