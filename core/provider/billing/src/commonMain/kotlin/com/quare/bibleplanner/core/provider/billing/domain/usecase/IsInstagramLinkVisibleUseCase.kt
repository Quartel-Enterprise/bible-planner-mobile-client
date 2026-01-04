package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsInstagramLinkVisibleUseCase {
    suspend operator fun invoke(): Boolean
}
