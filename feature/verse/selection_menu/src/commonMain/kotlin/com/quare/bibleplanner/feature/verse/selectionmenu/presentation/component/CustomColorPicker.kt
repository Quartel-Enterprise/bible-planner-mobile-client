package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.selection_menu.generated.resources.Res
import bibleplanner.feature.verse.selection_menu.generated.resources.apply_color
import bibleplanner.feature.verse.selection_menu.generated.resources.cancel
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.ui.component.colorpicker.HueLightnessPicker
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private const val MIN_LIGHTNESS = 30
private const val MAX_LIGHTNESS = 85
private const val MAX_LIGHTNESS_TRACK = 88
private const val SATURATION = 0.8f
private const val PREVIEW_RING_ALPHA = 0.12f
private val previewSize = 46.dp
private val previewRingWidth = 2.dp

// Why: saturation is fixed so every mix stays legible behind verse text.
@Composable
internal fun CustomColorPicker(
    color: CustomColorUiModel,
    onColorChange: (Int, Int) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(previewSize)
                        .clip(CircleShape)
                        .background(
                            HighlightColor
                                .Custom(
                                    hue = color.hue,
                                    lightness = color.lightness,
                                ).toSwatchColor(),
                        ).border(
                            width = previewRingWidth,
                            color = Color.Black.copy(alpha = PREVIEW_RING_ALPHA),
                            shape = CircleShape,
                        ),
                )
                HueLightnessPicker(
                    modifier = Modifier.weight(1f),
                    hue = color.hue,
                    lightness = color.lightness,
                    lightnessRange = MIN_LIGHTNESS..MAX_LIGHTNESS,
                    lightnessTrackEnd = MAX_LIGHTNESS_TRACK,
                    saturation = SATURATION,
                    onColorChange = onColorChange,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onApply,
                ) {
                    Text(text = stringResource(Res.string.apply_color))
                }
            }
        }
    }
}
