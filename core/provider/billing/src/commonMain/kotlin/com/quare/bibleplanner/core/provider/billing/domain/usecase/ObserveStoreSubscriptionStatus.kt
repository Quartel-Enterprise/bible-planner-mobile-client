package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

fun interface ObserveStoreSubscriptionStatus {
    operator fun invoke(): Flow<SubscriptionStatus>
}
