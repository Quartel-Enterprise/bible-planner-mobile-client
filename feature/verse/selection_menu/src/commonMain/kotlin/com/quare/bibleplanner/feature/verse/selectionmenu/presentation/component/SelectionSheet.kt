package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DISMISS_HEIGHT_FRACTION = 0.4f
private const val DISMISS_VELOCITY = 1_500f
private const val OFF_SCREEN_OFFSET_PX = 10_000f
private val cornerRadius = 28.dp
private val handleWidth = 36.dp
private val handleHeight = 4.dp
private val sheetElevation = 8.dp

/**
 * The selection tools: a bottom sheet over the chapter on a narrow window, a plain panel filling its
 * own pane on a wide one.
 *
 * Neither is modal — there is no scrim and the chapter stays tappable, so the selection can be
 * extended verse by verse while this is open. That rules out [androidx.compose.material3.ModalBottomSheet],
 * whose scrim would swallow those taps, so the drag it would have given us is built here instead:
 * the sheet follows the finger down and closes when thrown or dragged past its middle, and springs
 * back otherwise.
 */
@Composable
internal fun SelectionSheet(
    selection: VerseSelectionUiState,
    onEvent: (VerseSelectionUiEvent) -> Unit,
    isWide: Boolean,
    platform: Platform,
    modifier: Modifier = Modifier,
) {
    if (isWide) {
        SelectionSurface(
            modifier = modifier.fillMaxHeight(),
            isWide = true,
        ) {
            SelectionPanel(
                selection = selection,
                onEvent = onEvent,
                platform = platform,
            )
        }
        return
    }
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(OFF_SCREEN_OFFSET_PX) }
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    var isEntered by remember { mutableStateOf(false) }
    /*
     * The sheet waits well below the screen until it knows its own height, then slides up, and
     * leaves the same way: closing waits for the slide-out before the entry is actually popped.
     * The scene swap itself is instant, so this is the only motion the user sees.
     */
    LaunchedEffect(sheetHeightPx) {
        if (isEntered || sheetHeightPx == 0f) return@LaunchedEffect
        dragOffset.snapTo(sheetHeightPx)
        dragOffset.animateTo(0f)
        isEntered = true
    }
    val dismiss: () -> Unit = {
        coroutineScope.launch {
            dragOffset.animateTo(sheetHeightPx)
            onEvent(VerseSelectionUiEvent.OnClearSelectionClick)
        }
    }
    SelectionSurface(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> sheetHeightPx = size.height.toFloat() }
            .offset { IntOffset(x = 0, y = dragOffset.value.roundToInt()) }
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
        isWide = false,
    ) {
        DragHandle()
        SelectionPanel(
            selection = selection,
            onEvent = { event ->
                if (event == VerseSelectionUiEvent.OnClearSelectionClick) dismiss() else onEvent(event)
            },
            platform = platform,
        )
    }
}

@Composable
private fun SelectionSurface(
    modifier: Modifier,
    isWide: Boolean,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = if (isWide) {
            RectangleShape
        } else {
            RoundedCornerShape(
                topStart = cornerRadius,
                topEnd = cornerRadius,
            )
        },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (isWide) 0.dp else sheetElevation,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            content()
        }
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(handleWidth)
                .height(handleHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
