package com.quare.bibleplanner.android.feature.verseselection

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiState
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DISMISS_HEIGHT_FRACTION = 0.4f
private const val DISMISS_VELOCITY = 1_500f
private const val OFF_SCREEN_OFFSET_PX = 10_000f
private const val NARROW_SHEET_MAX_HEIGHT_FRACTION = 0.9f
private val cornerRadius = 28.dp
private val handleWidth = 36.dp
private val handleHeight = 4.dp
private val sheetElevation = 8.dp

@Composable
internal fun VerseSelectionContent(
    uiState: VerseSelectionUiState,
    isWideLayout: Boolean,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    if (isWideLayout) {
        VerseSelectionSidePane(
            uiState = uiState,
            onEvent = onEvent,
        )
    } else {
        VerseSelectionSheet(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun VerseSelectionSidePane(
    uiState: VerseSelectionUiState,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    val closeGuard = rememberSheetCloseGuard()
    SelectionSurface(
        modifier = Modifier
            .fillMaxHeight()
            .blockPointerInput(isBlocked = closeGuard.isClosing),
        shape = RectangleShape,
        shadowElevation = 0.dp,
    ) {
        VerseSelectionPanel(
            uiState = uiState,
            onEvent = { event ->
                if (event == VerseSelectionUiEvent.OnClearSelectionClick) {
                    closeGuard.close { onEvent(event) }
                } else {
                    onEvent(event)
                }
            },
        )
    }
}

/*
 * Why: not modal (no scrim) so the chapter stays tappable while extending the selection; that rules out
 * ModalBottomSheet, so its slide in and drag to dismiss are rebuilt here.
 */
@Composable
private fun VerseSelectionSheet(
    uiState: VerseSelectionUiState,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val closeGuard = rememberSheetCloseGuard()
    val dragOffset = remember { Animatable(OFF_SCREEN_OFFSET_PX) }
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    /*
     * Why: the scene swap is instant, so this slide is the only motion; it waits for its own height before entering,
     * and closing waits for the slide-out before the entry is popped. It runs once: the height grows when the palette
     * and annotations land, and restarting on it would snap the sheet back off screen mid-slide or cancel a dismiss.
     */
    LaunchedEffect(Unit) {
        val enterHeightPx = snapshotFlow { sheetHeightPx }.first { heightPx -> heightPx > 0f }
        dragOffset.snapTo(enterHeightPx)
        dragOffset.animateTo(0f)
    }
    val dismiss: () -> Unit = {
        closeGuard.close {
            coroutineScope.launch {
                dragOffset.animateTo(sheetHeightPx)
                onEvent(VerseSelectionUiEvent.OnClearSelectionClick)
            }
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        SelectionSurface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight * NARROW_SHEET_MAX_HEIGHT_FRACTION)
                .onSizeChanged { size -> sheetHeightPx = size.height.toFloat() }
                .offset {
                    IntOffset(
                        x = 0,
                        y = dragOffset.value.roundToInt(),
                    )
                }.blockPointerInput(isBlocked = closeGuard.isClosing)
                .draggable(
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f))
                        }
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        val isDismissed = velocity > DISMISS_VELOCITY ||
                            dragOffset.value > sheetHeightPx * DISMISS_HEIGHT_FRACTION
                        if (isDismissed) dismiss() else dragOffset.animateTo(0f)
                    },
                ),
            shape = RoundedCornerShape(
                topStart = cornerRadius,
                topEnd = cornerRadius,
            ),
            shadowElevation = sheetElevation,
        ) {
            SheetDragHandle()
            VerseSelectionPanel(
                uiState = uiState,
                onEvent = { event ->
                    if (event == VerseSelectionUiEvent.OnClearSelectionClick) dismiss() else onEvent(event)
                },
            )
        }
    }
}

@Composable
private fun SelectionSurface(
    modifier: Modifier,
    shape: Shape,
    shadowElevation: Dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = shadowElevation,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            content()
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 12.dp,
                bottom = 2.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(handleWidth)
                .height(handleHeight)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(percent = 50),
                ),
        )
    }
}
