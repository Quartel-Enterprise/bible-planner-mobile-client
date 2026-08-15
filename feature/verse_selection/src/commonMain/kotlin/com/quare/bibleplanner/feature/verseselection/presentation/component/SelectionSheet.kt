package com.quare.bibleplanner.feature.verseselection.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verseselection.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.feature.verseselection.presentation.model.VerseSelectionUiState

private val cornerRadius = 28.dp
private val handleWidth = 36.dp
private val handleHeight = 4.dp
private val sheetElevation = 8.dp

/**
 * The selection tools: a bottom sheet over the chapter on a narrow window, a plain panel filling its
 * own pane on a wide one.
 *
 * Neither is modal — there is no scrim and the chapter stays tappable, so the selection can be
 * extended verse by verse while this is open.
 */
@Composable
internal fun SelectionSheet(
    selection: VerseSelectionUiState,
    onEvent: (VerseSelectionUiEvent) -> Unit,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = if (isWide) modifier.fillMaxHeight() else modifier.fillMaxWidth(),
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
            if (!isWide) {
                DragHandle()
            }
            SelectionPanel(
                selection = selection,
                onEvent = onEvent,
            )
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
