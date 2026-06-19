package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveProVerificationRequired {
    operator fun invoke(): Flow<Boolean>
}
