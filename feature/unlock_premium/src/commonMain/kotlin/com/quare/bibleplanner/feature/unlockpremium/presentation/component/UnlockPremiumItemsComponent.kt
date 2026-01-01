package com.quare.bibleplanner.feature.unlockpremium.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.description.UnlockPremiumDescription
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.premiumicon.PremiumIcon
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiEvent
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun UnlockPremiumItemsComponent(
    modifier: Modifier = Modifier,
    uiState: UnlockPremiumUiState,
    onEvent: (UnlockPremiumUiEvent) -> Unit,
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
            UnlockPremiumHeadline(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            VerticalSpacer(8)
        }
        item {
            UnlockPremiumDescription(modifier = Modifier.padding(horizontal = 16.dp))
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
            UnlockPremiumFooter(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                onButtonClick = { onEvent(UnlockPremiumUiEvent.OnStartPremiumJourney) },
            )
        }
        item {
            VerticalSpacer(16)
        }
    }
}
