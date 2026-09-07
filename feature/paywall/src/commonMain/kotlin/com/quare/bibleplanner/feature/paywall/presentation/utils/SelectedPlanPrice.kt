package com.quare.bibleplanner.feature.paywall.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import org.jetbrains.compose.resources.stringResource

/**
 * Price of the plan the user is about to buy, shown next to the call to action — e.g. `$9.99/year`.
 */
@Composable
internal fun PaywallUiState.Success.selectedPlanPriceDescription(): String? =
    subscriptionPlans.firstOrNull { plan -> plan.isSelected }?.let { plan ->
        "${plan.priceDescription}/${stringResource(plan.periodUnit)}"
    }
