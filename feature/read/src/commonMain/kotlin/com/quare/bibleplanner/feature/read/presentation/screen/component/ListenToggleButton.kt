package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_listen
import bibleplanner.feature.read.generated.resources.listening_open_player
import org.jetbrains.compose.resources.stringResource

private const val SOFT_PRIMARY_ALPHA = 0.12f
private val iconSize = 22.dp

@Composable
internal fun ListenToggleButton(
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)
    }
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            modifier = Modifier.size(size),
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = contentColor),
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = if (isActive && isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.Headphones,
                contentDescription = stringResource(
                    if (isActive) Res.string.listening_open_player else Res.string.listening_listen,
                ),
            )
        }
    }
}
