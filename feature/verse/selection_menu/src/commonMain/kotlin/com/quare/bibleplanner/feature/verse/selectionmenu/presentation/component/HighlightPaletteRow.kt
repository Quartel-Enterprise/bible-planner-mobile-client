package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.selection_menu.generated.resources.Res
import bibleplanner.feature.verse.selection_menu.generated.resources.custom_color
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private val swatchSize = 34.dp
private val checkSize = 18.dp
private val customSwatchBrush = Brush.sweepGradient(
    listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFD166),
        Color(0xFF6BCB77),
        Color(0xFF4D96FF),
        Color(0xFFB388F5),
        Color(0xFFEC6FA8),
        Color(0xFFFF6B6B),
    ),
)

/**
 * The six preset colours, then whatever custom ones the user has mixed, then the swatch that opens
 * the picker. Tapping the colour the selection already carries is what removes the highlight, so the
 * active swatch is marked with a check rather than a separate "remove" button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HighlightPaletteRow(
    customColors: List<HighlightColor.Custom>,
    activeColor: HighlightColor?,
    onColorClick: (HighlightColor) -> Unit,
    onCustomColorLongClick: (HighlightColor.Custom) -> Unit,
    onCustomColorPickerOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        PresetHighlightColor.entries.forEach { preset ->
            val color = HighlightColor.Preset(preset)
            ColorSwatch(
                color = color.toSwatchColor(),
                isActive = activeColor?.key == color.key,
                onClick = { onColorClick(color) },
            )
        }
        customColors.forEach { custom ->
            ColorSwatch(
                modifier = Modifier.combinedClickable(
                    onClick = { onColorClick(custom) },
                    onLongClick = { onCustomColorLongClick(custom) },
                ),
                color = custom.toSwatchColor(),
                isActive = activeColor?.key == custom.key,
                onClick = null,
            )
        }
        val customColorLabel = stringResource(Res.string.custom_color)
        Box(
            modifier = Modifier
                .size(swatchSize)
                .clip(CircleShape)
                .background(customSwatchBrush)
                .combinedClickable(onClick = onCustomColorPickerOpen)
                .semantics { contentDescription = customColorLabel },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(checkSize),
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.scrim,
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isActive: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(swatchSize)
            .clip(CircleShape)
            .background(color)
            .then(
                onClick?.let { Modifier.combinedClickable(onClick = it) } ?: Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isActive) {
            Icon(
                modifier = Modifier.size(checkSize),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.scrim,
            )
        }
    }
}
