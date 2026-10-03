package com.quare.bibleplanner.feature.paywall.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaywallUiState.Success.selectedPlanPriceDescription(): String? =
    subscriptionPlans.firstOrNull { plan -> plan.isSelected }?.let { plan ->
        "${plan.priceDescription}/${stringResource(plan.periodUnit)}"
    }
