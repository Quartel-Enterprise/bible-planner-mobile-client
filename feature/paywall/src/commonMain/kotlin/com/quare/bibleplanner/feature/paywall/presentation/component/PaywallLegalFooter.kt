package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.paywall.presentation.component.description.SecurePaymentDescription
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun PaywallLegalFooter(
    storeName: String,
    onEvent: (PaywallUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RestorePurchaseComponent(onEvent = onEvent)
        VerticalSpacer(8)
        SecurePaymentDescription(storeName = storeName)
    }
}
