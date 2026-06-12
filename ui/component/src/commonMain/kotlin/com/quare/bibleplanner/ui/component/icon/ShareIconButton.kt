package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.provider.platform.Platform

@Composable
fun ShareIconButton(
    platform: Platform,
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val icon = when (platform) {
        Platform.Ios,
        Platform.Desktop.MacOs,
        -> Icons.Default.IosShare

        Platform.Desktop.Linux,
        Platform.Desktop.Windows,
        Platform.Android,
        -> Icons.Default.Share
    }
    CommonIconButton(
        modifier = modifier,
        imageVector = icon,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
