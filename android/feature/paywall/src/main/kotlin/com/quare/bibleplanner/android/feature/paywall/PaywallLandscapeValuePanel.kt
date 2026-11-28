package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.paywall.presentation.model.PremiumFeature

private val backButtonBottomSpacing = 18.dp
private val heroSubtitleMaxWidth = 290.dp

@Composable
internal fun PaywallLandscapeValuePanel(
    premiumFeatures: List<PremiumFeature>,
    dimensions: PaywallLandscapeDimensions,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimensions.panelPaddingStart,
                top = dimensions.panelPaddingTop,
                end = dimensions.panelPaddingEnd,
                bottom = dimensions.panelPaddingBottom,
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            val heroIcon = dimensions.heroIcon
            if (heroIcon == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PaywallBackButton(onClick = onBackClick)
                    Column {
                        BecomeProTitle(
                            fontSize = dimensions.heroTitleFontSize,
                            titleColor = contentColor,
                            proColor = contentColor,
                        )
                        Text(
                            text = stringResource(R.string.paywall_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                        )
                    }
                }
            } else {
                PaywallBackButton(onClick = onBackClick)
                Spacer(modifier = Modifier.height(backButtonBottomSpacing))
                Column(modifier = Modifier.padding(start = dimensions.panelContentStartPadding)) {
                    Box(
                        modifier = Modifier
                            .size(heroIcon.boxSize)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(heroIcon.boxCornerRadius),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            modifier = Modifier.size(heroIcon.iconSize),
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = contentColor,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BecomeProTitle(
                        fontSize = dimensions.heroTitleFontSize,
                        titleColor = contentColor,
                        proColor = contentColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.widthIn(max = heroSubtitleMaxWidth),
                        text = stringResource(R.string.paywall_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensions.heroBottomSpacing))
        PremiumFeatureList(
            modifier = Modifier.padding(start = dimensions.panelContentStartPadding),
            premiumFeatures = premiumFeatures,
            titleColor = contentColor,
            subtitleColor = contentColor,
            itemSpacing = dimensions.featureSpacing,
            iconSize = dimensions.featureIconSize,
        )
    }
}
