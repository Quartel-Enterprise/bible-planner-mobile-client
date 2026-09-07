package com.quare.bibleplanner.feature.paywall.presentation.component.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.SubscriptionPlanColumnCard
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanDiscountBadge
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubscriptionPlansRow(
    subscriptionPlans: List<SubscriptionPlanPresentationModel>,
    onEvent: (PaywallUiEvent) -> Unit,
    itemSpacing: Dp,
    priceFontSize: TextUnit,
    priceUnitFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        subscriptionPlans.forEach { plan ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                SubscriptionPlanColumnCard(
                    modifier = Modifier.fillMaxHeight(),
                    title = stringResource(plan.title),
                    description = stringResource(plan.description),
                    price = plan.priceDescription,
                    periodUnit = stringResource(plan.periodUnit),
                    priceFontSize = priceFontSize,
                    priceUnitFontSize = priceUnitFontSize,
                    isSelected = plan.isSelected,
                    onClick = { onEvent(PaywallUiEvent.OnPlanSelected(plan.type)) },
                )
                plan.savePercentage?.let { savePercentage ->
                    SubscriptionPlanDiscountBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-12).dp,
                                y = (-10).dp,
                            ),
                        savePercentage = savePercentage,
                    )
                }
            }
        }
    }
}
