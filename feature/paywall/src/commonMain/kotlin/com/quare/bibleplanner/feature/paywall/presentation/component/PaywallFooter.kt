package com.quare.bibleplanner.feature.paywall.presentation.component

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
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun PaywallFooter(
    storeName: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StartProButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isLoading = isLoading,
            onClick = { onEvent(PaywallUiEvent.OnStartProJourneyClick) },
        )
        VerticalSpacer(12)
        RestorePurchaseComponent(onEvent = onEvent)
        VerticalSpacer(8)
        SecurePaymentDescription(storeName = storeName)
    }
}
