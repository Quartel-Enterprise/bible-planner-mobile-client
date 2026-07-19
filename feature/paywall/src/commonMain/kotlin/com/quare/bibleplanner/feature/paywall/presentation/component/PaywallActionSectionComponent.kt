package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState

@Composable
internal fun PaywallActionSectionComponent(
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
    buttonHeight: Dp,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PaywallUiState.Success -> PaywallFooter(
            modifier = modifier.fillMaxWidth(),
            storeName = uiState.storeName,
            isLoading = uiState.isPurchasing,
            buttonHeight = buttonHeight,
            onEvent = onEvent,
        )

        PaywallUiState.Loading -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        PaywallUiState.Error -> PaywallErrorCard(modifier = modifier)
    }
}
