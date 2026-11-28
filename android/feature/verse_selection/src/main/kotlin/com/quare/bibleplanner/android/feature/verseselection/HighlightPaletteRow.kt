package com.quare.bibleplanner.android.feature.verseselection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor

private val swatchSize = 34.dp
private val checkSize = 18.dp
private val lockBadgeSize = 16.dp
private val lockIconSize = 12.dp
private val customSwatchRingWidth = 1.5.dp
private const val LOCKED_CUSTOM_SWATCH_ALPHA = 0.75f
private const val LOCKED_PRESET_ALPHA = 0.42f
private const val CUSTOM_SWATCH_RING_ALPHA = 0.35f
private const val PRESET_LOCK_ICON_ALPHA = 0.6f
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
private val lockedCustomSwatchBrush = Brush.sweepGradient(
    listOf(
        Color(0xFF8F7F7F),
        Color(0xFFA09479),
        Color(0xFF7F9B83),
        Color(0xFF7B8AA8),
        Color(0xFF8F83A3),
        Color(0xFFA4849A),
        Color(0xFF8F7F7F),
    ),
)

// Why: tapping the active colour removes the highlight, so it is marked with a check instead of a remove button.
@Composable
internal fun HighlightPaletteRow(
    customColors: List<HighlightColor.Custom>,
    activeColor: HighlightColor?,
    isProUser: Boolean,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        FlowRow(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            PresetHighlightColor.entries.forEach { preset ->
                val color = HighlightColor.Preset(preset)
                val isLocked = preset.requiresPro && !isProUser
                ColorSwatchBox(
                    color = color.toSwatchColor(),
                    isActive = activeColor?.key == color.key,
                    isLocked = isLocked,
                    onClick = {
                        onEvent(
                            if (isLocked) {
                                VerseSelectionUiEvent.OnLockedColorClick
                            } else {
                                VerseSelectionUiEvent.OnHighlightColorClick(color)
                            },
                        )
                    },
                )
            }
            customColors.forEach { custom ->
                ColorSwatchBox(
                    color = custom.toSwatchColor(),
                    isActive = activeColor?.key == custom.key,
                    isLocked = false,
                    onClick = { onEvent(VerseSelectionUiEvent.OnHighlightColorClick(custom)) },
                    onLongClick = { onEvent(VerseSelectionUiEvent.OnCustomColorLongClick(custom)) },
                )
            }
            CustomColorSwatchBox(
                isLocked = !isProUser,
                onClick = { onEvent(VerseSelectionUiEvent.OnCustomColorPickerOpen) },
            )
        }
    }
}

@Composable
private fun ColorSwatchBox(
    color: Color,
    isActive: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .size(swatchSize)
            .clip(CircleShape)
            .background(color)
            .alpha(if (isLocked) LOCKED_PRESET_ALPHA else 1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLocked) {
            Icon(
                modifier = Modifier.size(checkSize),
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Black.copy(alpha = PRESET_LOCK_ICON_ALPHA),
            )
        } else if (isActive) {
            Icon(
                modifier = Modifier.size(checkSize),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.scrim,
            )
        }
    }
}

@Composable
private fun CustomColorSwatchBox(
    isLocked: Boolean,
    onClick: () -> Unit,
) {
    val customColorLabel = stringResource(R.string.verse_selection_custom_color)
    // Why: only the inner circle is clipped, so the lock badge can overhang its edge.
    Box(modifier = Modifier.size(swatchSize)) {
        Box(
            modifier = Modifier
                .size(swatchSize)
                .alpha(if (isLocked) LOCKED_CUSTOM_SWATCH_ALPHA else 1f)
                .clip(CircleShape)
                .combinedClickable(onClick = onClick)
                .semantics { contentDescription = customColorLabel }
                .background(if (isLocked) lockedCustomSwatchBrush else customSwatchBrush)
                .border(
                    width = customSwatchRingWidth,
                    color = Color.White.copy(alpha = CUSTOM_SWATCH_RING_ALPHA),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(checkSize),
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = Color.White,
            )
        }
        if (isLocked) {
            LockBadge(modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun LockBadge(modifier: Modifier) {
    Box(
        modifier = modifier
            .offset(
                x = 2.dp,
                y = 2.dp,
            ).size(lockBadgeSize)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(lockIconSize),
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
