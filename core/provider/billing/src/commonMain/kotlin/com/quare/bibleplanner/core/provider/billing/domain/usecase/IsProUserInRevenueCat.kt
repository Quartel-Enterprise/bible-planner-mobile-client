package com.quare.bibleplanner.core.provider.billing.domain.usecase

fun interface IsProUserInRevenueCat {
    suspend operator fun invoke(): Boolean
}
