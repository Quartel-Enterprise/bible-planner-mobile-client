package com.quare.bibleplanner.feature.unlockpremium.presentation.model

import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanType
import org.jetbrains.compose.resources.StringResource

data class SubscriptionPlanPresentationModel(
    val title: StringResource,
    val period: StringResource,
    val savePercentage: Int?,
    val isSelected: Boolean,
    val price: Double,
    val type: SubscriptionPlanType,
)
