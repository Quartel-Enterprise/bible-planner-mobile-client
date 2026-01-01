package com.quare.bibleplanner.feature.unlockpremium.presentation.component.description

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.unlock_premium.generated.resources.Res
import bibleplanner.feature.unlock_premium.generated.resources.unlock_premium_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UnlockPremiumDescription(modifier: Modifier = Modifier) {
    BaseTextDescription(
        modifier = modifier,
        text = stringResource(Res.string.unlock_premium_description),
    )
}
