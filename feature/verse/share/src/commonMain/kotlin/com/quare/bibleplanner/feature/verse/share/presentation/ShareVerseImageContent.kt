package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_action
import bibleplanner.feature.verse.share.generated.resources.share_background
import bibleplanner.feature.verse.share.generated.resources.share_typography
import com.quare.bibleplanner.core.image.encodeToPng
import com.quare.bibleplanner.feature.verse.share.presentation.component.ShareVerseCard
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val cardWidth = 242.dp
private val swatchSize = 32.dp
private val fontTileSize = 42.dp

@Composable
internal fun ShareVerseImageContent(
    uiState: ShareVerseUiState,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShareVerseCard(
            modifier = Modifier
                .width(cardWidth)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                },
            uiState = uiState,
        )
        SectionLabel(text = stringResource(Res.string.share_typography))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareCardFont.entries.forEach { font ->
                Surface(
                    modifier = Modifier.size(fontTileSize),
                    shape = RoundedCornerShape(10.dp),
                    color = if (font == uiState.font) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Box(
                        modifier = Modifier.clickable { onEvent(ShareVerseUiEvent.OnFontClick(font)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Aa",
                            fontFamily = font.toFontFamily(),
                        )
                    }
                }
            }
        }
        SectionLabel(text = stringResource(Res.string.share_background))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareCardBackground.entries.forEach { background ->
                Box(
                    modifier = Modifier
                        .size(swatchSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(background.startColor, background.endColor)),
                        ).border(
                            width = if (background == uiState.background) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ).clickable { onEvent(ShareVerseUiEvent.OnBackgroundClick(background)) },
                )
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isReady,
            onClick = {
                coroutineScope.launch {
                    val imageBytes = graphicsLayer.toImageBitmap().encodeToPng()
                    onEvent(ShareVerseUiEvent.OnShareImageReady(imageBytes))
                }
            },
        ) {
            Text(text = stringResource(Res.string.share_action))
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
