package com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.feature.paywall.presentation.model.PremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.utils.toSubtextArgs
import com.quare.bibleplanner.feature.paywall.presentation.utils.toSubtextResource
import com.quare.bibleplanner.feature.paywall.presentation.utils.toTitleResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PremiumFeaturesList(
    premiumFeatures: List<PremiumFeature>,
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
        premiumFeatures.forEach { feature ->
            PremiumFeatureItem(
                text = stringResource(feature.toTitleResource()),
                subtext = stringResource(feature.toSubtextResource(), *feature.toSubtextArgs().toTypedArray()),
                titleColor = titleColor,
                subtitleColor = subtitleColor,
                iconSize = iconSize,
            )
        }
    }
}
