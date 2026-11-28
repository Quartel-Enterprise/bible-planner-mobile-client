package com.quare.bibleplanner.feature.paywall.presentation.model

import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType

data class SubscriptionPlanPresentationModel(
    val savePercentage: Int?,
    val isSelected: Boolean,
    val priceDescription: String,
    val type: SubscriptionPlanType,
)
