package com.quare.bibleplanner.feature.paywall.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.paywall_subtitle
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.ui.component.icon.BackIcon
import org.jetbrains.compose.resources.stringResource

private val titleFontSize = 22.sp
private val badgeSize = 40.dp
private val badgeCornerRadius = 13.dp
private val badgeIconSize = 22.dp
private val badgeEndPadding = 4.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun PaywallTopBar(
    platform: Platform,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                BecomeProTitle(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    fontSize = titleFontSize,
                    titleColor = MaterialTheme.colorScheme.onSurface,
                    proColor = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.paywall_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            BackIcon(platform = platform, onBackClick = onBackClick)
        },
        actions = {
            PaywallPremiumBadge(modifier = Modifier.padding(end = badgeEndPadding))
        },
    )
}

@Composable
private fun PaywallPremiumBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(badgeCornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(badgeIconSize),
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
