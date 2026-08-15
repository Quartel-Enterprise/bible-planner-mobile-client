package com.quare.bibleplanner.feature.verseselection.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val THUMB_BORDER_ALPHA = 0.18f
private val trackHeight = 14.dp
private val thumbSize = 22.dp
private val thumbBorderWidth = 2.dp
private val thumbElevation = 2.dp

/**
 * A slider whose track *is* the value being picked: the gradient shows every colour on offer, so
 * there is no active/inactive split to draw and no tick marks to read. The thumb is a plain white
 * knob, which stays visible over any hue the track runs through.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColorPickerSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackBrush: Brush,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    /*
     * Forced to the thumb's height so the two sliders can sit as close as the design puts them: the
     * stock slider reserves a 48dp touch row, which on its own pushed them far apart.
     */
    Slider(
        modifier = modifier.requiredHeight(thumbSize),
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(trackBrush),
            )
        },
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .shadow(
                        elevation = thumbElevation,
                        shape = CircleShape,
                    ).background(
                        color = Color.White,
                        shape = CircleShape,
                    ).border(
                        width = thumbBorderWidth,
                        color = Color.Black.copy(alpha = THUMB_BORDER_ALPHA),
                        shape = CircleShape,
                    ),
            )
        },
    )
}
