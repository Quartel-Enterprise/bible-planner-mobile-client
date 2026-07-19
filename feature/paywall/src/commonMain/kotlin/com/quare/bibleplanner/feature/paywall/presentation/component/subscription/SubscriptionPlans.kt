package com.quare.bibleplanner.feature.paywall.presentation.component.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.SubscriptionPlanCard
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanDiscountBadge
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubscriptionPlans(
    subscriptionPlans: List<SubscriptionPlanPresentationModel>,
    onEvent: (PaywallUiEvent) -> Unit,
    itemSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        subscriptionPlans.forEach { plan ->
            Box {
                SubscriptionPlanCard(
                    title = stringResource(plan.title),
                    description = stringResource(plan.description),
                    price = plan.priceDescription,
                    period = stringResource(plan.period),
                    isSelected = plan.isSelected,
                    onClick = { onEvent(PaywallUiEvent.OnPlanSelected(plan.type)) },
                )
                plan.savePercentage?.let { savePercentage ->
                    SubscriptionPlanDiscountBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-16).dp,
                                y = (-10).dp,
                            ),
                        savePercentage = savePercentage,
                    )
                }
            }
        }
    }
}
