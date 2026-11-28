package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.core.image.encodeToPng
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val cardWidth = 242.dp
private val wideCardSlotWidth = 300.dp
private val wideSheetMaxWidth = 760.dp
private val wideContentGap = 20.dp
private val fontTileSize = 42.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareVerseImageContent(
    uiState: ShareVerseUiState,
    isWideLayout: Boolean,
    actionGuard: ShareImageActionGuard,
    snackbarHostState: SnackbarHostState,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(R.string.share_verse_image_title),
        onDismiss = { onEvent(ShareVerseUiEvent.OnDismiss) },
        sheetMaxWidth = if (isWideLayout) wideSheetMaxWidth else BottomSheetDefaults.SheetMaxWidth,
        snackbarHostState = snackbarHostState,
    ) {
        // Why: the sheet is its own window, so the layer must come from the context of the window that draws the card.
        val graphicsLayer = rememberGraphicsLayer()
        if (isWideLayout) {
            ShareImageWideLayout(
                uiState = uiState,
                graphicsLayer = graphicsLayer,
                actionGuard = actionGuard,
                onEvent = onEvent,
            )
        } else {
            ShareImageNarrowLayout(
                uiState = uiState,
                graphicsLayer = graphicsLayer,
                actionGuard = actionGuard,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ShareImageNarrowLayout(
    uiState: ShareVerseUiState,
    graphicsLayer: GraphicsLayer,
    actionGuard: ShareImageActionGuard,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
        ShareBackgroundSection(
            uiState = uiState,
            onEvent = onEvent,
        )
        ShareActionsRow(
            modifier = Modifier.fillMaxWidth(),
            isReady = uiState.isReady,
            graphicsLayer = graphicsLayer,
            actionGuard = actionGuard,
            onEvent = onEvent,
        )
    }
}

// Why: the card keeps its fixed portrait width on wide screens; only the controls grow beside it.
@Composable
private fun ShareImageWideLayout(
    uiState: ShareVerseUiState,
    graphicsLayer: GraphicsLayer,
    actionGuard: ShareImageActionGuard,
    onEvent: (ShareVerseUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            ShareBackgroundSection(
                modifier = Modifier.padding(top = 16.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
            ShareActionsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                isReady = uiState.isReady,
                graphicsLayer = graphicsLayer,
                actionGuard = actionGuard,
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
                    if (uiState.isStreakShown) R.string.share_verse_remove_streak else R.string.share_verse_add_streak,
                ),
                imageVector = Icons.Default.LocalFireDepartment,
                onClick = { onEvent(ShareVerseUiEvent.OnStreakToggleClick) },
            )
        }
        CardToggleChip(
            label = stringResource(
                if (uiState.isWatermarkShown) {
                    R.string.share_verse_remove_watermark
                } else {
                    R.string.share_verse_show_watermark
                },
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
        ShareSectionLabel(text = stringResource(R.string.share_verse_typography))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareCardFont.entries.forEach { entry ->
                val isSelected = entry == font
                Surface(
                    modifier = Modifier.size(fontTileSize),
                    selected = isSelected,
                    onClick = { onFontClick(entry) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
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
private fun ShareActionsRow(
    isReady: Boolean,
    graphicsLayer: GraphicsLayer,
    actionGuard: ShareImageActionGuard,
    onEvent: (ShareVerseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    /*
     * Why: PNG compression of a full card takes long enough to drop frames on the main thread, and a failed
     * capture must lift the guard, or Share and Save would stay dead for the rest of the screen.
     */
    val captureCard: ((ByteArray) -> ShareVerseUiEvent) -> Unit = { toEvent ->
        coroutineScope.launch {
            suspendRunCatching {
                val imageBitmap = graphicsLayer.toImageBitmap()
                withContext(Dispatchers.Default) { imageBitmap.encodeToPng() }
            }.onSuccess { imageBytes -> onEvent(toEvent(imageBytes)) }
                .onFailure {
                    actionGuard.isSharing = false
                    actionGuard.isSaving = false
                }
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
            onClick = {
                if (!actionGuard.isBusy) {
                    actionGuard.isSharing = true
                    captureCard(ShareVerseUiEvent::OnShareImageReady)
                }
            },
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Default.Share,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.share_verse_action))
        }
        FilledTonalIconButton(
            enabled = isReady,
            onClick = {
                if (!actionGuard.isBusy) {
                    actionGuard.isSaving = true
                    captureCard(ShareVerseUiEvent::OnDownloadImageReady)
                }
            },
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = stringResource(R.string.share_verse_download),
            )
        }
    }
}

@Composable
internal fun ShareSectionLabel(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
