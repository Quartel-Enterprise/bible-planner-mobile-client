package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_action
import bibleplanner.feature.verse.share.generated.resources.share_add_streak
import bibleplanner.feature.verse.share.generated.resources.share_background
import bibleplanner.feature.verse.share.generated.resources.share_custom_color
import bibleplanner.feature.verse.share.generated.resources.share_download
import bibleplanner.feature.verse.share.generated.resources.share_photo_background
import bibleplanner.feature.verse.share.generated.resources.share_pro_only
import bibleplanner.feature.verse.share.generated.resources.share_remove_streak
import bibleplanner.feature.verse.share.generated.resources.share_remove_watermark
import bibleplanner.feature.verse.share.generated.resources.share_show_watermark
import bibleplanner.feature.verse.share.generated.resources.share_typography
import com.quare.bibleplanner.core.image.encodeToPng
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.share.presentation.component.ShareVerseCard
import com.quare.bibleplanner.feature.verse.share.presentation.mapper.toSwatchColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.component.colorpicker.HueLightnessPicker
import com.quare.bibleplanner.ui.component.colorpicker.hueColorStops
import com.quare.bibleplanner.ui.component.icon.shareIcon
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val LOCKED_SWATCH_ALPHA = 0.45f
private val cardWidth = 242.dp
private val wideCardSlotWidth = 300.dp
private val wideContentPadding = 18.dp
private val wideContentGap = 20.dp
private val swatchSize = 32.dp
private val swatchLockSize = 14.dp
private val swatchIconSize = 18.dp
private val swatchDash = floatArrayOf(6f, 5f)
private val fontTileSize = 42.dp
private val customSwatchBrush = Brush.sweepGradient(hueColorStops)

@Composable
internal fun ShareVerseImageContent(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    platform: Platform,
    modifier: Modifier = Modifier,
) {
    val graphicsLayer = rememberGraphicsLayer()
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CapturedShareCard(
            modifier = Modifier.width(cardWidth),
            uiState = uiState,
            graphicsLayer = graphicsLayer,
        )
        CardTogglesRow(
            uiState = uiState,
            onEvent = onEvent,
        )
        TypographySection(
            font = uiState.font,
            onFontClick = { font -> onEvent(ShareVerseUiEvent.OnFontClick(font)) },
        )
        BackgroundSection(
            uiState = uiState,
            onEvent = onEvent,
        )
        ShareActionsRow(
            modifier = Modifier.fillMaxWidth(),
            platform = platform,
            isReady = uiState.isReady,
            graphicsLayer = graphicsLayer,
            onEvent = onEvent,
        )
    }
}

/*
 * Why: the card keeps the design fixed 300dp portrait width on wide dialogs; only the controls
 * grow, matching the desktop composer.
 */
@Composable
internal fun ShareVerseImageWideContent(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    platform: Platform,
    modifier: Modifier = Modifier,
) {
    val graphicsLayer = rememberGraphicsLayer()
    Row(
        modifier = modifier.padding(wideContentPadding),
        horizontalArrangement = Arrangement.spacedBy(wideContentGap),
    ) {
        Box(
            modifier = Modifier.width(wideCardSlotWidth),
            contentAlignment = Alignment.Center,
        ) {
            CapturedShareCard(
                modifier = Modifier.width(cardWidth),
                uiState = uiState,
                graphicsLayer = graphicsLayer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.reference,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = uiState.versionAbbreviation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            CardTogglesRow(
                modifier = Modifier.padding(top = 12.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
            TypographySection(
                modifier = Modifier.padding(top = 12.dp),
                font = uiState.font,
                onFontClick = { font -> onEvent(ShareVerseUiEvent.OnFontClick(font)) },
            )
            BackgroundSection(
                modifier = Modifier.padding(top = 16.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
            Spacer(modifier = Modifier.weight(1f))
            ShareActionsRow(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                platform = platform,
                isReady = uiState.isReady,
                graphicsLayer = graphicsLayer,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun CapturedShareCard(
    uiState: ShareVerseUiState,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    ShareVerseCard(
        modifier = modifier.drawWithContent {
            graphicsLayer.record { this@drawWithContent.drawContent() }
            drawLayer(graphicsLayer)
        },
        uiState = uiState,
    )
}

@Composable
private fun CardTogglesRow(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        if (uiState.streakDays > 0) {
            CardToggleChip(
                label = stringResource(
                    if (uiState.isStreakShown) Res.string.share_remove_streak else Res.string.share_add_streak,
                ),
                imageVector = Icons.Default.LocalFireDepartment,
                onClick = { onEvent(ShareVerseUiEvent.OnStreakToggleClick) },
            )
        }
        CardToggleChip(
            label = stringResource(
                if (uiState.isWatermarkShown) Res.string.share_remove_watermark else Res.string.share_show_watermark,
            ),
            imageVector = when {
                !uiState.isProUser -> Icons.Default.Lock
                uiState.isWatermarkShown -> Icons.Default.VisibilityOff
                else -> Icons.Default.Visibility
            },
            onClick = { onEvent(ShareVerseUiEvent.OnWatermarkToggleClick) },
        )
    }
}

@Composable
private fun CardToggleChip(
    label: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = imageVector,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun TypographySection(
    font: ShareCardFont,
    onFontClick: (ShareCardFont) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(text = stringResource(Res.string.share_typography))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareCardFont.entries.forEach { entry ->
                Surface(
                    modifier = Modifier.size(fontTileSize),
                    shape = RoundedCornerShape(10.dp),
                    color = if (entry == font) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Box(
                        modifier = Modifier.clickable { onFontClick(entry) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Aa",
                            fontFamily = entry.toFontFamily(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundSection(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = uiState.background
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionLabel(text = stringResource(Res.string.share_background))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShareCardBackground.entries.forEach { entry ->
                SwatchButton(
                    isSelected = background == ShareCardBackgroundUiModel.Preset(entry),
                    contentDescription = null,
                    onClick = { onEvent(ShareVerseUiEvent.OnBackgroundClick(entry)) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(entry.startColor, entry.endColor))),
                    )
                }
            }
            LockableSwatchButton(
                isSelected = background is ShareCardBackgroundUiModel.Custom,
                isLocked = !uiState.isProUser,
                contentDescription = stringResource(Res.string.share_custom_color),
                onClick = { onEvent(ShareVerseUiEvent.OnCustomColorClick) },
            ) {
                Box(modifier = Modifier.fillMaxSize().background(customSwatchBrush))
            }
            LockableSwatchButton(
                isSelected = background is ShareCardBackgroundUiModel.Photo,
                isLocked = !uiState.isProUser,
                contentDescription = stringResource(Res.string.share_photo_background),
                onClick = { onEvent(ShareVerseUiEvent.OnPhotoBackgroundClick) },
            ) {
                PhotoSwatchContent(background = background)
            }
        }
        if (uiState.isColorPickerOpen) {
            CustomColorPanel(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun PhotoSwatchContent(background: ShareCardBackgroundUiModel) {
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
    val proOnly = stringResource(Res.string.share_pro_only)
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
            ).clickable(onClick = onClick)
            .semantics {
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
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    val color = uiState.customColor
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
                    .clip(CircleShape)
                    .background(color.toSwatchColor()),
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

@Composable
private fun ShareActionsRow(
    platform: Platform,
    isReady: Boolean,
    graphicsLayer: GraphicsLayer,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val captureCard: ((ByteArray) -> ShareVerseUiEvent) -> Unit = { toEvent ->
        coroutineScope.launch {
            val imageBytes = graphicsLayer.toImageBitmap().encodeToPng()
            onEvent(toEvent(imageBytes))
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            modifier = Modifier.weight(1f),
            enabled = isReady,
            onClick = { captureCard(ShareVerseUiEvent::OnShareImageReady) },
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = platform.shareIcon,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.share_action))
        }
        FilledTonalIconButton(
            enabled = isReady,
            onClick = { captureCard(ShareVerseUiEvent::OnDownloadImageReady) },
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = stringResource(Res.string.share_download),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
