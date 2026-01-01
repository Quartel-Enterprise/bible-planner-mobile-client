package com.quare.bibleplanner.feature.unlockpremium.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.unlock_premium.generated.resources.Res
import bibleplanner.feature.unlock_premium.generated.resources.unlock_premium_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UnlockPremiumHeadline(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.unlock_premium_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
}
