package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.component.description.SecurePaymentDescription
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.StartProButton
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent

@Composable
internal fun PaywallFooter(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StartProButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isLoading = isLoading,
            onClick = { onEvent(PaywallUiEvent.OnStartProJourneyClick) },
        )
        RestorePurchaseComponent(onEvent = onEvent)
        SecurePaymentDescription()
    }
}
