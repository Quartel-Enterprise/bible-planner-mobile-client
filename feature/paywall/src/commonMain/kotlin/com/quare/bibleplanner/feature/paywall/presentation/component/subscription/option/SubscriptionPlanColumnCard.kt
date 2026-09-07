package com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanIcon
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanSurface
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

/**
 * Plan option laid out as a column — radio and name on top, the price standing out below it.
 * Used side by side with the other plans when the screen is wide enough for it.
 */
@Composable
internal fun SubscriptionPlanColumnCard(
    title: String,
    description: String,
    price: String,
    periodUnit: String,
    priceFontSize: TextUnit,
    priceUnitFontSize: TextUnit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubscriptionPlanSurface(
        modifier = modifier,
        isSelected = isSelected,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SubscriptionPlanIcon(isSelected = isSelected)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            VerticalSpacer(10)
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontSize = priceFontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        ),
                    ) {
                        append(price)
                    }
                    withStyle(
                        style = SpanStyle(
                            fontSize = priceUnitFontSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        append(" /$periodUnit")
                    }
                },
                maxLines = 1,
                softWrap = false,
            )
            VerticalSpacer(4)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
