package com.quare.bibleplanner.ui.icons

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon as MaterialIcon

@Composable
fun Icon(
    icon: AppIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    MaterialIcon(
        painter = rememberAppIconPainter(icon),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
