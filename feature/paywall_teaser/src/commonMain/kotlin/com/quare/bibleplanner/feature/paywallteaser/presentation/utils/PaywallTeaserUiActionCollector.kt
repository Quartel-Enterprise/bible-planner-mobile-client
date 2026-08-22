package com.quare.bibleplanner.feature.paywallteaser.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun PaywallTeaserUiActionCollector(
    uiActionFlow: Flow<PaywallTeaserUiAction>,
    reason: PaywallTeaserReason,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            PaywallTeaserUiAction.NavigateBack -> onNavigateBack()

            PaywallTeaserUiAction.NavigateToPaywall -> onNavigateReplacingTop(
                PaywallNavRoute(reason.toPaywallEntrySource()),
            )
        }
    }
}

private fun PaywallTeaserReason.toPaywallEntrySource(): PaywallEntrySource = when (this) {
    PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR -> PaywallEntrySource.HIGHLIGHT_CUSTOM_COLOR
}
