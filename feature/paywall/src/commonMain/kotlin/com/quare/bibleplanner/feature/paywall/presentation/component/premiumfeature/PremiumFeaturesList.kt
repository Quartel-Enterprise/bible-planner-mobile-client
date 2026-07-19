package com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.feature_ai_day_study
import bibleplanner.feature.paywall.generated.resources.feature_ai_day_study_subtext
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes
import bibleplanner.feature.paywall.generated.resources.feature_unlimited_notes_subtext
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PremiumFeaturesList(
    maxFreeNotes: Int?,
    itemSpacing: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        PremiumFeatureItem(
            text = stringResource(Res.string.feature_ai_day_study),
            subtext = stringResource(Res.string.feature_ai_day_study_subtext),
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            iconSize = iconSize,
        )
        PremiumFeatureItem(
            text = stringResource(Res.string.feature_unlimited_notes),
            subtext = maxFreeNotes?.let { stringResource(Res.string.feature_unlimited_notes_subtext, it) },
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            iconSize = iconSize,
        )
    }
}
