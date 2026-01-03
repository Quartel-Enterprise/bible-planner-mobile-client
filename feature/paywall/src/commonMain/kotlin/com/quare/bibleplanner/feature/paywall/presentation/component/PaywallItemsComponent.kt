package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.description.PaywallDescription
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumicon.PremiumIcon
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun PaywallItemsComponent(
    modifier: Modifier = Modifier,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            VerticalSpacer(16)
        }
        item {
            PremiumIcon()
        }
        item {
            VerticalSpacer(24)
        }
        item {
            PaywallHeadline(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            VerticalSpacer(8)
        }
        item {
            PaywallDescription(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            VerticalSpacer(32)
        }
        item {
            PremiumFeaturesList(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            VerticalSpacer(32)
        }
        item {
            SubscriptionPlans(
                modifier = Modifier.padding(horizontal = 16.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
        }
        item {
            VerticalSpacer(24)
        }
        item {
            val isPurchasing = (uiState as? PaywallUiState.Success)?.isPurchasing ?: false
            PaywallFooter(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                isLoading = isPurchasing,
                onButtonClick = { onEvent(PaywallUiEvent.OnStartPremiumJourneyClick) },
            )
        }
        item {
            VerticalSpacer(16)
        }
    }
}
