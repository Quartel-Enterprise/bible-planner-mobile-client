package com.quare.bibleplanner.feature.unlockpremium.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.description.SecurePaymentDescription
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.subscription.StartPremiumButton

@Composable
internal fun UnlockPremiumFooter(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StartPremiumButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onButtonClick,
        )
        SecurePaymentDescription()
    }
}
