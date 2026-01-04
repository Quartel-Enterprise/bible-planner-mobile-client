package com.quare.bibleplanner.feature.paywall.presentation.component.subscription

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.SubscriptionPlanCard
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.option.component.SubscriptionPlanDiscountBadge
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubscriptionPlans(
    modifier: Modifier = Modifier,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        when (uiState) {
            is PaywallUiState.Success -> {
                val subscriptionPlans = uiState.subscriptionPlans
                subscriptionPlans.forEachIndexed { index, plan ->
                    Box {
                        SubscriptionPlanCard(
                            title = stringResource(plan.title),
                            price = "${plan.priceDescription} / ${stringResource(plan.period)}",
                            isSelected = plan.isSelected,
                            onClick = { onEvent(PaywallUiEvent.OnPlanSelected(plan.type)) },
                        )
                        plan.savePercentage?.let { savePercentage ->
                            SubscriptionPlanDiscountBadge(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(
                                        y = (-8).dp,
                                        x = 16.dp,
                                    ),
                                savePercentage = savePercentage,
                            )
                        }
                    }
                    if (!subscriptionPlans.isLastIndex(index)) {
                        VerticalSpacer(16)
                    }
                }
            }

            is PaywallUiState.Error -> {
                // Handle error state if needed
            }

            PaywallUiState.Loading -> {
            }
        }
    }
}
