package com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.feature_ad_free
import bibleplanner.feature.paywall.generated.resources.feature_support_development
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes_subtext
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PremiumFeaturesList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        PremiumFeatureItem(
            text = stringResource(Res.string.feature_unlimited_notes),
            subtext = stringResource(Res.string.feature_unlimited_notes_subtext),
        )
        PremiumFeatureItem(
            text = stringResource(Res.string.feature_support_development),
        )
        PremiumFeatureItem(
            text = stringResource(Res.string.feature_ad_free),
        )
    }
}
