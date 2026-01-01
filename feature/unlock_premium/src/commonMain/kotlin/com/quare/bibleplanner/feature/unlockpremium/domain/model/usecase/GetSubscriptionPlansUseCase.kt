package com.quare.bibleplanner.feature.unlockpremium.domain.model.usecase

import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanModel
import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanType

class GetSubscriptionPlansUseCase {
    operator fun invoke(): List<SubscriptionPlanModel> = listOf(
        SubscriptionPlanModel(
            planType = SubscriptionPlanType.Monthly,
            price = 1.0,
            savePercentage = null,
        ),
        SubscriptionPlanModel(
            planType = SubscriptionPlanType.Annual,
            price = 10.0,
            savePercentage = 20,
        ),
    )
}
