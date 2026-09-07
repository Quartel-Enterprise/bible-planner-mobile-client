package com.quare.bibleplanner.feature.paywall.presentation.component.premiumicon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

private const val CHECK_SIZE_FRACTION = 0.64f

@Composable
internal fun PremiumCheckIcon(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(size * CHECK_SIZE_FRACTION),
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
