package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_subtitle
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.paywall.presentation.component.premiumfeature.PremiumFeaturesList
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallLandscapeDimensions
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private const val BACK_ICON_BOTTOM_SPACING = 18

/**
 * Value proposition beside the plans when the screen is wide: what Pro is and what it unlocks.
 * A phone in landscape opens it with a compact bar, while a desktop-sized window gets the full hero.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PaywallLandscapeValuePanel(
    platform: Platform,
    maxFreeNotes: Int?,
    dimensions: PaywallLandscapeDimensions,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
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
                    BackIcon(platform = platform, onBackClick = onBackClick)
                    Column {
                        BecomeProTitle(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            fontSize = dimensions.heroTitleFontSize,
                            titleColor = contentColor,
                            proColor = contentColor,
                        )
                        Text(
                            text = stringResource(Res.string.paywall_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                        )
                    }
                }
            } else {
                BackIcon(platform = platform, onBackClick = onBackClick)
                VerticalSpacer(BACK_ICON_BOTTOM_SPACING)
                PaywallHero(
                    modifier = Modifier.padding(start = dimensions.panelContentStartPadding),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    titleFontSize = dimensions.heroTitleFontSize,
                    titleColor = contentColor,
                    proColor = contentColor,
                    subtitleColor = contentColor,
                    iconBoxSize = heroIcon.boxSize,
                    iconBoxCornerRadius = heroIcon.boxCornerRadius,
                    iconBoxColor = MaterialTheme.colorScheme.surface,
                    iconSize = heroIcon.iconSize,
                    iconTint = contentColor,
                    horizontalAlignment = Alignment.Start,
                    textAlign = TextAlign.Start,
                )
            }
        }
        VerticalSpacer(dimensions.heroBottomSpacing)
        PremiumFeaturesList(
            modifier = Modifier.padding(start = dimensions.panelContentStartPadding),
            maxFreeNotes = maxFreeNotes,
            titleColor = contentColor,
            subtitleColor = contentColor,
            itemSpacing = dimensions.featureSpacing,
            iconSize = dimensions.featureIconSize,
        )
    }
}
