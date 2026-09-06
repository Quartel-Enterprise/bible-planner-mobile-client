package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_1
import bibleplanner.feature.paywall.generated.resources.paywall_title_part_2
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BecomeProTitle(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    fontSize: TextUnit,
    titleColor: Color,
    proColor: Color,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        Row(modifier = modifier) {
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "become_pro_part_1"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
                text = stringResource(Res.string.paywall_title_part_1),
                fontSize = fontSize,
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
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = proColor,
            )
        }
    }
}
