package com.quare.bibleplanner.feature.read.presentation.screen.component.selection

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.apply_color
import bibleplanner.feature.read.generated.resources.cancel
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.read.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.feature.read.presentation.utils.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private const val MIN_HUE = 0f
private const val MAX_HUE = 360f
private const val MIN_LIGHTNESS = 30f
private const val MAX_LIGHTNESS = 85f
private const val SATURATION = 0.8f
private const val PREVIEW_RING_ALPHA = 0.12f
private const val PERCENT = 100f
private const val MAX_LIGHTNESS_TRACK = 88f
private val previewSize = 46.dp
private val previewRingWidth = 2.dp
private val hueBrush = Brush.horizontalGradient(
    listOf(
        Color(0xFFFF5555),
        Color(0xFFFFFF55),
        Color(0xFF55FF55),
        Color(0xFF55FFFF),
        Color(0xFF5555FF),
        Color(0xFFFF55FF),
        Color(0xFFFF5555),
    ),
)

/**
 * Hue and lightness only: saturation is fixed so every mix stays legible behind verse text.
 *
 * Each track is painted in the colours it selects — the full hue wheel, and the lightness ramp of
 * the hue the user is on — so the sliders read as the choice they offer rather than as a value.
 */
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ColorPickerSlider(
                        value = color.hue.toFloat(),
                        valueRange = MIN_HUE..MAX_HUE,
                        trackBrush = hueBrush,
                        onValueChange = { hue ->
                            onColorChange(hue.toInt(), color.lightness)
                        },
                    )
                    ColorPickerSlider(
                        value = color.lightness.toFloat(),
                        valueRange = MIN_LIGHTNESS..MAX_LIGHTNESS,
                        trackBrush = color.hue.toLightnessBrush(),
                        onValueChange = { lightness ->
                            onColorChange(color.hue, lightness.toInt())
                        },
                    )
                }
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

/** The lightness ramp is drawn in the hue the user is on, so the track previews the actual choice. */
private fun Int.toLightnessBrush(): Brush = Brush.horizontalGradient(
    listOf(
        Color.hsl(
            hue = toFloat(),
            saturation = SATURATION,
            lightness = MIN_LIGHTNESS / PERCENT,
        ),
        Color.hsl(
            hue = toFloat(),
            saturation = SATURATION,
            lightness = MAX_LIGHTNESS_TRACK / PERCENT,
        ),
    ),
)
