package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_subtitle
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_1
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_2
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PaywallHero(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    titleFontSize: TextUnit,
    titleColor: Color,
    proColor: Color,
    subtitleColor: Color,
    iconBoxSize: Dp,
    iconBoxCornerRadius: Dp,
    iconBoxColor: Color,
    iconSize: Dp,
    iconTint: Color,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    textAlign: TextAlign = TextAlign.Center,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .background(
                    color = iconBoxColor,
                    shape = RoundedCornerShape(iconBoxCornerRadius),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = iconTint,
            )
        }
        VerticalSpacer(16)
        with(sharedTransitionScope) {
            Row {
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "become_pro_part_1"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                    text = stringResource(Res.string.paywall_title_part_1),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                HorizontalSpacer(6)
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "become_pro_part_2"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                    text = stringResource(Res.string.paywall_title_part_2),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = proColor,
                )
            }
        }
        VerticalSpacer(8)
        Text(
            modifier = Modifier.widthIn(max = 290.dp),
            text = stringResource(Res.string.paywall_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
            textAlign = textAlign,
        )
    }
}
