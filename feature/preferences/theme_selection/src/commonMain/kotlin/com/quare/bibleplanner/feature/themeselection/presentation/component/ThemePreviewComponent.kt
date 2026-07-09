package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.theme.LocalDynamicColorScheme
import com.quare.bibleplanner.ui.theme.color.darkScheme
import com.quare.bibleplanner.ui.theme.color.lightScheme
import com.quare.bibleplanner.ui.theme.model.Theme

@Composable
internal fun ThemePreviewComponent(
    theme: Theme,
    modifier: Modifier = Modifier,
) {
    val resolveDynamicScheme = LocalDynamicColorScheme.current
    val colors = theme.toPreviewColors(
        lightScheme = resolveDynamicScheme(false) ?: lightScheme,
        darkScheme = resolveDynamicScheme(true) ?: darkScheme,
    )
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PreviewLine(
            color = colors.firstLine,
            widthFraction = 0.55f,
            height = 7.dp,
        )
        PreviewLine(
            color = colors.otherLines,
            widthFraction = 0.85f,
            height = 5.dp,
        )
        PreviewLine(
            color = colors.otherLines,
            widthFraction = 0.7f,
            height = 5.dp,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(
                    width = 36.dp,
                    height = 12.dp,
                ).clip(RoundedCornerShape(6.dp))
                .background(colors.accent),
        )
    }
}

@Composable
private fun PreviewLine(
    color: Color,
    widthFraction: Float,
    height: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

private fun Theme.toPreviewColors(
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
): ThemePreviewColors = when (this) {
    Theme.LIGHT -> ThemePreviewColors(
        background = SolidColor(lightScheme.surfaceContainerHigh),
        firstLine = lightScheme.onSurfaceVariant,
        otherLines = lightScheme.onSurfaceVariant.copy(alpha = 0.7f),
        accent = lightScheme.primary,
    )

    Theme.DARK -> ThemePreviewColors(
        background = SolidColor(darkScheme.surfaceContainerHigh),
        firstLine = darkScheme.onSurfaceVariant,
        otherLines = darkScheme.onSurfaceVariant.copy(alpha = 0.7f),
        accent = darkScheme.primary,
    )

    Theme.SYSTEM -> ThemePreviewColors(
        background = Brush.linearGradient(
            0.5f to lightScheme.surfaceContainerHigh,
            0.5f to darkScheme.surfaceContainerHigh,
        ),
        firstLine = lightScheme.outline,
        otherLines = lightScheme.outline.copy(alpha = 0.7f),
        accent = lightScheme.primary,
    )
}
