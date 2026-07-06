package com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SubscriptionPlanContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    price: String,
    period: String,
    isSelected: Boolean,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubscriptionPlanIcon(isSelected = isSelected)
            SubscriptionPlanInformation(
                title = title,
                description = description,
            )
        }
        SubscriptionPlanPrice(
            price = price,
            period = period,
        )
    }
}
