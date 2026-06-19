package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveIsProUser {
    operator fun invoke(): Flow<Boolean>
}
