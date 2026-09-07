package com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanContent
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanSurface

@Composable
internal fun SubscriptionPlanCard(
    title: String,
    description: String,
    price: String,
    period: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    SubscriptionPlanSurface(
        modifier = Modifier.fillMaxWidth(),
        isSelected = isSelected,
        onClick = onClick,
    ) {
        SubscriptionPlanContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = title,
            description = description,
            price = price,
            period = period,
            isSelected = isSelected,
        )
    }
}
