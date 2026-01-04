package com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanContent

private const val ANIMATION_TIME_MILLIS = 350

@Composable
internal fun SubscriptionPlanCard(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = ANIMATION_TIME_MILLIS),
        label = "containerColor",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        },
        animationSpec = tween(durationMillis = ANIMATION_TIME_MILLIS),
        label = "borderColor",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor,
        ),
        onClick = onClick,
    ) {
        SubscriptionPlanContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = title,
            price = price,
            isSelected = isSelected,
        )
    }
}
