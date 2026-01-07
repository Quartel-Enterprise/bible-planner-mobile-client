package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

fun interface GetSubscriptionStatusFlowUseCase {
    operator fun invoke(): Flow<SubscriptionStatus?>
}
