package com.quare.bibleplanner.feature.read.presentation.screen.component.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val previewSize = 46.dp

/** Hue and lightness only: saturation is fixed so every mix stays legible behind verse text. */
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
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .size(previewSize)
                        .clip(CircleShape)
                        .background(
                            HighlightColor
                                .Custom(
                                    hue = color.hue,
                                    lightness = color.lightness,
                                ).toSwatchColor(),
                        ),
                ) {}
                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = color.hue.toFloat(),
                        onValueChange = { hue ->
                            onColorChange(hue.toInt(), color.lightness)
                        },
                        valueRange = MIN_HUE..MAX_HUE,
                    )
                    Slider(
                        value = color.lightness.toFloat(),
                        onValueChange = { lightness ->
                            onColorChange(color.hue, lightness.toInt())
                        },
                        valueRange = MIN_LIGHTNESS..MAX_LIGHTNESS,
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
