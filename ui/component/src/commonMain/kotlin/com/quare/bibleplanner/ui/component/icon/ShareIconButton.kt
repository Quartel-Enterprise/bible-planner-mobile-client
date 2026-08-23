package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.quare.bibleplanner.core.provider.platform.Platform

val Platform.shareIcon: ImageVector
    get() = when (this) {
        Platform.Ios,
        Platform.Desktop.MacOs,
        -> Icons.Default.IosShare

        Platform.Desktop.Linux,
        Platform.Desktop.Windows,
        Platform.Android,
        -> Icons.Default.Share
    }

@Composable
fun ShareIconButton(
    platform: Platform,
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit,
) {
    CommonIconButton(
        modifier = modifier,
        imageVector = platform.shareIcon,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
