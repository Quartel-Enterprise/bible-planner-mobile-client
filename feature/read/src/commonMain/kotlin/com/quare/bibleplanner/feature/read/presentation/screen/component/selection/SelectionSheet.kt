package com.quare.bibleplanner.feature.read.presentation.screen.component.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadSelectionUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

private val cornerRadius = 28.dp
private val handleWidth = 36.dp
private val handleHeight = 4.dp
private val sheetElevation = 8.dp

/**
 * The selection tools as a bottom sheet, taking the bottom bar's place while a selection is live.
 *
 * Deliberately not a modal sheet: there is no scrim and the chapter stays tappable underneath, so
 * the selection can be extended verse by verse while the sheet is open.
 */
@Composable
internal fun SelectionSheet(
    selection: ReadSelectionUiModel,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = sheetElevation,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
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
            SelectionPanel(
                selection = selection,
                onEvent = onEvent,
            )
        }
    }
}
