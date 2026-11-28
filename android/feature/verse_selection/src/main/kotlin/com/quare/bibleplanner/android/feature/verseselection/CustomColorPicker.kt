package com.quare.bibleplanner.android.feature.verseselection

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.ui.component.colorpicker.HueLightnessPicker
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor

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
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(previewSize)
                        .background(
                            color = HighlightColor
                                .Custom(
                                    hue = color.hue,
                                    lightness = color.lightness,
                                ).toSwatchColor(),
                            shape = CircleShape,
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
                    onColorChange = { hue, lightness ->
                        onEvent(
                            VerseSelectionUiEvent.OnCustomColorChange(
                                hue = hue,
                                lightness = lightness,
                            ),
                        )
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(VerseSelectionUiEvent.OnCustomColorCancelClick) },
                ) {
                    Text(text = stringResource(R.string.verse_selection_cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(VerseSelectionUiEvent.OnCustomColorApplyClick) },
                ) {
                    Text(text = stringResource(R.string.verse_selection_apply_color))
                }
            }
        }
    }
}
