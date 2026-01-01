package com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.option.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun SubscriptionPlanContent(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    isSelected: Boolean,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubscriptionPlanInformation(
            title = title,
            price = price,
        )
        SubscriptionPlanIcon(isSelected = isSelected)
    }
}
