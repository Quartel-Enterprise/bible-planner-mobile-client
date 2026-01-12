package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.getPlatform

@Composable
fun ShareIconButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val icon = when (getPlatform()) {
        Platform.IOS,
        Platform.MACOS,
        -> Icons.Default.IosShare

        Platform.LINUX,
        Platform.WINDOWS,
        Platform.ANDROID,
        -> Icons.Default.Share
    }
    CommonIconButton(
        modifier = modifier,
        imageVector = icon,
        contentDescription = "Share",
        onClick = onClick,
    )
}
