package com.quare.bibleplanner.feature.paywall.presentation.model

import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import org.jetbrains.compose.resources.StringResource

data class SubscriptionPlanPresentationModel(
    val title: StringResource,
    val period: StringResource,
    val savePercentage: Int?,
    val isSelected: Boolean,
    val priceDescription: String,
    val type: SubscriptionPlanType,
)
