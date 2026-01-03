package com.quare.bibleplanner.feature.paywall.presentation.component.description

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaywallDescription(modifier: Modifier = Modifier) {
    BaseTextDescription(
        modifier = modifier,
        text = stringResource(Res.string.paywall_description),
    )
}
