package com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.option.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.unlock_premium.generated.resources.Res
import bibleplanner.feature.unlock_premium.generated.resources.save_percent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubscriptionPlanDiscountBadge(
    modifier: Modifier = Modifier,
    savePercentage: Int,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(4.dp),
            ).padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.save_percent, savePercentage),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            fontWeight = FontWeight.Bold,
        )
    }
}
