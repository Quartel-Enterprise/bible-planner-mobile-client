package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.component.colorpicker.HueLightnessPicker
import com.quare.bibleplanner.ui.component.colorpicker.hueColorStops

private const val LOCKED_SWATCH_ALPHA = 0.45f
private val swatchSize = 32.dp
private val swatchLockSize = 14.dp
private val swatchIconSize = 18.dp
private val swatchDash = floatArrayOf(6f, 5f)
private val customSwatchBrush = Brush.sweepGradient(hueColorStops)

@Composable
internal fun ShareBackgroundSection(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = uiState.background
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShareSectionLabel(text = stringResource(R.string.share_verse_background))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShareCardBackground.entries.forEach { entry ->
                val preset = ShareCardBackgroundUiModel.Preset(entry)
                val palette = preset.toPalette()
                SwatchButton(
                    isSelected = background == preset,
                    contentDescription = null,
                    onClick = { onEvent(ShareVerseUiEvent.OnBackgroundClick(entry)) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(palette.startColor, palette.endColor))),
                    )
                }
            }
            LockableSwatchButton(
                isSelected = background is ShareCardBackgroundUiModel.Custom,
                isLocked = !uiState.isProUser,
                contentDescription = stringResource(R.string.share_verse_custom_color),
                onClick = { onEvent(ShareVerseUiEvent.OnCustomColorClick) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(customSwatchBrush),
                )
            }
            LockableSwatchButton(
                isSelected = background is ShareCardBackgroundUiModel.Photo,
                isLocked = !uiState.isProUser,
                contentDescription = stringResource(R.string.share_verse_photo_background),
                onClick = { onEvent(ShareVerseUiEvent.OnPhotoBackgroundClick) },
            ) {
                PhotoSwatchImage(background = background)
            }
        }
        if (uiState.isColorPickerOpen) {
            CustomColorPanel(
                color = uiState.customColor,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun PhotoSwatchImage(background: ShareCardBackgroundUiModel) {
    if (background is ShareCardBackgroundUiModel.Photo) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = background.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    } else {
        val outlineColor = MaterialTheme.colorScheme.outline
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = outlineColor,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(swatchDash),
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(swatchIconSize),
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LockableSwatchButton(
    isSelected: Boolean,
    isLocked: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val proOnly = stringResource(R.string.share_verse_pro_only)
    SwatchButton(
        isSelected = isSelected,
        contentDescription = if (isLocked) "$contentDescription, $proOnly" else contentDescription,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = LOCKED_SWATCH_ALPHA)),
                )
                Icon(
                    modifier = Modifier.size(swatchLockSize),
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SwatchButton(
    isSelected: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(swatchSize)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ).clickable(
                role = Role.Button,
                onClick = onClick,
            ).semantics {
                selected = isSelected
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
    ) {
        content()
    }
}

@Composable
private fun CustomColorPanel(
    color: ShareCardCustomColor,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(swatchSize)
                    .background(
                        color = color.toSwatchColor(),
                        shape = CircleShape,
                    ),
            )
            HueLightnessPicker(
                modifier = Modifier.weight(1f),
                hue = color.hue,
                lightness = color.lightness,
                lightnessRange = ShareCardCustomColor.MIN_LIGHTNESS..ShareCardCustomColor.MAX_LIGHTNESS,
                lightnessTrackEnd = ShareCardCustomColor.MAX_LIGHTNESS,
                saturation = ShareCardCustomColor.SATURATION,
                onColorChange = { hue, lightness ->
                    onEvent(
                        ShareVerseUiEvent.OnCustomColorChange(
                            hue = hue,
                            lightness = lightness,
                        ),
                    )
                },
            )
        }
    }
}
