package com.quare.bibleplanner.ui.component.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val MIN_HUE = 0f
private const val MAX_HUE = 360f
private const val PERCENT = 100f
private val sliderSpacing = 4.dp
val hueColorStops: List<Color> = listOf(
    Color(0xFFFF5555),
    Color(0xFFFFFF55),
    Color(0xFF55FF55),
    Color(0xFF55FFFF),
    Color(0xFF5555FF),
    Color(0xFFFF55FF),
    Color(0xFFFF5555),
)
private val hueBrush = Brush.horizontalGradient(hueColorStops)

/*
 * Why: the 4dp spacer gives the design's 12dp track gap because each thumb overhangs its track by 4dp;
 * the lightness track may run past the range so its light end doesn't look washed out at the thumb.
 */
@Composable
fun HueLightnessPicker(
    hue: Int,
    lightness: Int,
    lightnessRange: IntRange,
    lightnessTrackEnd: Int,
    saturation: Float,
    onColorChange: (hue: Int, lightness: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(sliderSpacing),
    ) {
        ColorPickerSlider(
            value = hue.toFloat(),
            valueRange = MIN_HUE..MAX_HUE,
            trackBrush = hueBrush,
            onValueChange = { newHue -> onColorChange(newHue.toInt(), lightness) },
        )
        ColorPickerSlider(
            value = lightness.toFloat(),
            valueRange = lightnessRange.first.toFloat()..lightnessRange.last.toFloat(),
            trackBrush = Brush.horizontalGradient(
                listOf(lightnessRange.first, lightnessTrackEnd).map { stop ->
                    Color.hsl(
                        hue = hue.toFloat(),
                        saturation = saturation,
                        lightness = stop / PERCENT,
                    )
                },
            ),
            onValueChange = { newLightness -> onColorChange(hue, newLightness.toInt()) },
        )
    }
}
