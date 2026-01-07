package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_error_message
import com.quare.bibleplanner.feature.paywall.presentation.component.description.PaywallDescription
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumicon.PremiumIcon
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

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
            when (uiState) {
                is PaywallUiState.Success -> {
                    val isPurchasing = uiState.isPurchasing
                    PaywallFooter(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        isLoading = isPurchasing,
                        onButtonClick = { onEvent(PaywallUiEvent.OnStartProJourneyClick) },
                    )
                    VerticalSpacer(16)
                }

                PaywallUiState.Loading -> {
                    CircularProgressIndicator()
                }

                PaywallUiState.Error -> {
                    val errorColor = MaterialTheme.colorScheme.error
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = errorColor,
                        ),
                    ) {
                        Text(
                            text = stringResource(Res.string.paywall_error_message),
                            modifier = Modifier.padding(16.dp),
                            color = errorColor,
                        )
                    }
                }
            }
        }
    }
}
