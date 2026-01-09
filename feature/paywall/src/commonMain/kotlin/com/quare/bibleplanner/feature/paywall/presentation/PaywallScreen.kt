package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallActionSectionComponent
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.SubscriptionPlans
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    snackbarHostState: SnackbarHostState,
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    titleContent: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    titleContent()
                },
                navigationIcon = {
                    BackIcon(onBackClick = { onEvent(PaywallUiEvent.OnBackClick) })
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
    ) { paddingValues ->
        ResponsiveColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            maxContentWidth = 800.dp,
            portraitContent = { portrait(uiState, onEvent) },
            landscapeContent = {
                landscape(uiState, onEvent)
            },
        )
    }
}

private fun ResponsiveContentScope.portrait(
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    responsiveItem {
        PremiumFeaturesList(modifier = Modifier.padding(horizontal = 16.dp))
    }
    responsiveItem {
        VerticalSpacer(16)
    }
    responsiveItem {
        SubscriptionPlans(
            modifier = Modifier.padding(horizontal = 16.dp),
            uiState = uiState,
            onEvent = onEvent,
        )
    }
    responsiveItem {
        VerticalSpacer(24)
    }
    responsiveItem {
        PaywallActionSectionComponent(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}

private fun ResponsiveContentScope.landscape(
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    responsiveItem {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PremiumFeaturesList()
                VerticalSpacer(16)
                SubscriptionPlans(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PaywallActionSectionComponent(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}
