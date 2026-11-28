package com.quare.bibleplanner.android.feature.verseselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiState

@Composable
internal fun VerseSelectionPanel(
    uiState: VerseSelectionUiState,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Why: the label is centred on the sheet, not on the space left by the close button.
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = verseReferenceLabel(
                    bookId = uiState.chapter.bookId,
                    chapterNumber = uiState.chapter.chapterNumber,
                    verseNumbers = uiState.verseNumbers,
                ),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { onEvent(VerseSelectionUiEvent.OnClearSelectionClick) },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.verse_selection_close),
                )
            }
        }
        HighlightPaletteRow(
            customColors = uiState.customColors,
            activeColor = uiState.activeColor,
            isProUser = uiState.isProUser,
            onEvent = onEvent,
        )
        if (uiState.customColors.isNotEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.verse_selection_custom_color_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        uiState.customColorPicker?.let { picker ->
            CustomColorPicker(
                color = picker,
                onEvent = onEvent,
            )
        }
        SelectionActionsRow(
            isSelectionSaved = uiState.isSelectionSaved,
            hasNote = uiState.note != null,
            onEvent = onEvent,
        )
    }
}
