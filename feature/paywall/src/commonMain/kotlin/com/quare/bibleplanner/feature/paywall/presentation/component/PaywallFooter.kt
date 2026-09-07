package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.feature.paywall.presentation.component.subscription.StartProButton
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun PaywallFooter(
    storeName: String,
    buttonHeight: Dp,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    priceDescription: String? = null,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StartProButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            isLoading = isLoading,
            priceDescription = priceDescription,
            onClick = { onEvent(PaywallUiEvent.OnStartProJourneyClick) },
        )
        VerticalSpacer(12)
        PaywallLegalFooter(
            storeName = storeName,
            onEvent = onEvent,
        )
    }
}
