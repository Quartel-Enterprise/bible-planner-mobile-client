package com.quare.bibleplanner.android.feature.themeselection

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionModel
import com.quare.bibleplanner.ui.theme.LocalDynamicColorScheme
import com.quare.bibleplanner.ui.theme.color.darkScheme
import com.quare.bibleplanner.ui.theme.color.lightScheme

private const val SELECTED_CONTAINER_ALPHA = 0.12f
private const val OTHER_LINES_ALPHA = 0.7f

@Composable
internal fun ThemeOptionCard(
    model: ThemeSelectionModel,
    onClick: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = model.isActive
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = modifier
            .clip(shape)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = { onClick(model.preference) },
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primary.copy(alpha = SELECTED_CONTAINER_ALPHA) else Color.Transparent,
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ThemePreviewBox(theme = model.preference)
                if (isSelected) {
                    SelectedBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp),
                    )
                }
            }
            ThemeOptionLabel(
                theme = model.preference,
                isSelected = isSelected,
            )
        }
    }
}

@Composable
private fun ThemeOptionLabel(
    theme: Theme,
    isSelected: Boolean,
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 5.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = theme.toIcon(),
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = stringResource(theme.toTitleRes()),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SelectedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(13.dp),
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

// Why: the preview draws each theme in the app's own schemes, Material You's when it is on.
@Composable
private fun ThemePreviewBox(theme: Theme) {
    val resolveDynamicScheme = LocalDynamicColorScheme.current
    val colors = theme.toPreviewColors(
        lightScheme = resolveDynamicScheme(false) ?: lightScheme,
        darkScheme = resolveDynamicScheme(true) ?: darkScheme,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
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

private class ThemePreviewColors(
    val background: Brush,
    val firstLine: Color,
    val otherLines: Color,
    val accent: Color,
)

private fun Theme.toPreviewColors(
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
): ThemePreviewColors = when (this) {
    Theme.LIGHT -> lightScheme.toSolidPreviewColors()

    Theme.DARK -> darkScheme.toSolidPreviewColors()

    Theme.SYSTEM -> ThemePreviewColors(
        background = Brush.linearGradient(
            0.5f to lightScheme.surfaceContainerHigh,
            0.5f to darkScheme.surfaceContainerHigh,
        ),
        firstLine = lightScheme.outline,
        otherLines = lightScheme.outline.copy(alpha = OTHER_LINES_ALPHA),
        accent = lightScheme.primary,
    )
}

private fun ColorScheme.toSolidPreviewColors(): ThemePreviewColors = ThemePreviewColors(
    background = SolidColor(surfaceContainerHigh),
    firstLine = onSurfaceVariant,
    otherLines = onSurfaceVariant.copy(alpha = OTHER_LINES_ALPHA),
    accent = primary,
)

@StringRes
private fun Theme.toTitleRes(): Int = when (this) {
    Theme.LIGHT -> R.string.theme_selection_light
    Theme.DARK -> R.string.theme_selection_dark
    Theme.SYSTEM -> R.string.theme_selection_system
}

private fun Theme.toIcon(): ImageVector = when (this) {
    Theme.LIGHT -> Icons.Filled.LightMode
    Theme.DARK -> Icons.Filled.DarkMode
    Theme.SYSTEM -> Icons.Filled.Devices
}
