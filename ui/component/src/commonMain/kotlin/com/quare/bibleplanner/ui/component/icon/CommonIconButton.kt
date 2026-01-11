package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A reusable composable wrapper around [IconButton] and [Icon] that standardizes
 * the appearance and accessibility of icon-only buttons across the app.
 *
 * This version uses an [ImageVector].
 */
@Composable
fun CommonIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

/**
 * A reusable composable wrapper around [IconButton] and [Icon] that standardizes
 * the appearance and accessibility of icon-only buttons across the app.
 *
 * This version uses a [Painter].
 */
@Composable
fun CommonIconButton(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
