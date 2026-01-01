package com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.core.utils.toMoneyFormat
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.option.SubscriptionPlanCard
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.option.component.SubscriptionPlanDiscountBadge
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiEvent
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubscriptionPlans(
    modifier: Modifier = Modifier,
    uiState: UnlockPremiumUiState,
    onEvent: (UnlockPremiumUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        val subscriptionPlans = uiState.subscriptionPlans
        subscriptionPlans.forEachIndexed { index, plan ->
            Box {
                SubscriptionPlanCard(
                    title = stringResource(plan.title),
                    price = "$${plan.price.toMoneyFormat()} / ${stringResource(plan.period)}",
                    isSelected = plan.isSelected,
                    onClick = { onEvent(UnlockPremiumUiEvent.OnPlanSelected(plan.type)) },
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
}
