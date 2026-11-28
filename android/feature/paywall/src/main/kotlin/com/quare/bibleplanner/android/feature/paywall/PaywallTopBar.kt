package com.quare.bibleplanner.android.feature.paywall

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val titleFontSize = 22.sp
private val badgeSize = 40.dp
private val badgeCornerRadius = 13.dp
private val badgeIconSize = 22.dp
private val badgeEndPadding = 4.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaywallTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                BecomeProTitle(
                    fontSize = titleFontSize,
                    titleColor = MaterialTheme.colorScheme.onSurface,
                    proColor = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.paywall_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = { PaywallBackButton(onClick = onBackClick) },
        actions = { PaywallPremiumBadge(modifier = Modifier.padding(end = badgeEndPadding)) },
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
