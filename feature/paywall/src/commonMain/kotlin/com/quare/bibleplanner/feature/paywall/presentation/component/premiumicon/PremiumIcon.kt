package com.quare.bibleplanner.feature.paywall.presentation.component.premiumicon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PremiumIcon() {
    Box(
        modifier = Modifier.size(80.dp),
    ) {
        PremiumBookIcon(
            modifier = Modifier.size(80.dp),
        )
        PremiumBadgeIcon(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x = 4.dp,
                    y = 4.dp,
                ),
        )
    }
}
