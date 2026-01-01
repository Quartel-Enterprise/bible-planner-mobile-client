package com.quare.bibleplanner.feature.unlockpremium.domain.model.plan

data class SubscriptionPlanModel(
    val planType: SubscriptionPlanType,
    val price: Double,
    val savePercentage: Int?,
)
