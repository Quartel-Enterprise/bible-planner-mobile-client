package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_action
import bibleplanner.feature.verse.share.generated.resources.share_background
import bibleplanner.feature.verse.share.generated.resources.share_typography
import com.quare.bibleplanner.core.image.encodeToPng
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.share.presentation.component.ShareVerseCard
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.component.icon.shareIcon
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val cardWidth = 242.dp
private val wideCardSlotWidth = 300.dp
private val wideContentPadding = 18.dp
private val wideContentGap = 20.dp
private val swatchSize = 32.dp
private val fontTileSize = 42.dp

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
        TypographySection(
            font = uiState.font,
            onFontClick = { font -> onEvent(ShareVerseUiEvent.OnFontClick(font)) },
        )
        BackgroundSection(
            background = uiState.background,
            onBackgroundClick = { background -> onEvent(ShareVerseUiEvent.OnBackgroundClick(background)) },
        )
        ShareImageButton(
            modifier = Modifier.fillMaxWidth(),
            platform = platform,
            isReady = uiState.isReady,
            graphicsLayer = graphicsLayer,
            onEvent = onEvent,
        )
    }
}

/**
 * The card keeps its portrait shape and the design's fixed 300dp column width even on a wide
 * dialog — only the controls beside it grow, matching how the desktop composer is laid out.
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
            TypographySection(
                modifier = Modifier.padding(top = 18.dp),
                font = uiState.font,
                onFontClick = { font -> onEvent(ShareVerseUiEvent.OnFontClick(font)) },
            )
            BackgroundSection(
                modifier = Modifier.padding(top = 16.dp),
                background = uiState.background,
                onBackgroundClick = { background -> onEvent(ShareVerseUiEvent.OnBackgroundClick(background)) },
            )
            Spacer(modifier = Modifier.weight(1f))
            ShareImageButton(
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
    background: ShareCardBackground,
    onBackgroundClick: (ShareCardBackground) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(text = stringResource(Res.string.share_background))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareCardBackground.entries.forEach { entry ->
                Box(
                    modifier = Modifier
                        .size(swatchSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(entry.startColor, entry.endColor)),
                        ).border(
                            width = if (entry == background) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ).clickable { onBackgroundClick(entry) },
                )
            }
        }
    }
}

@Composable
private fun ShareImageButton(
    platform: Platform,
    isReady: Boolean,
    graphicsLayer: GraphicsLayer,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Button(
        modifier = modifier,
        enabled = isReady,
        onClick = {
            coroutineScope.launch {
                val imageBytes = graphicsLayer.toImageBitmap().encodeToPng()
                onEvent(ShareVerseUiEvent.OnShareImageReady(imageBytes))
            }
        },
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.IconSize),
            imageVector = platform.shareIcon,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = stringResource(Res.string.share_action))
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
