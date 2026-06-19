package com.quare.bibleplanner.core.provider.billing.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveInstagramLinkVisible {
    operator fun invoke(): Flow<Boolean>
}
