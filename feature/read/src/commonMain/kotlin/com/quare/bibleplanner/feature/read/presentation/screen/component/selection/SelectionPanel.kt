package com.quare.bibleplanner.feature.read.presentation.screen.component.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.close
import bibleplanner.feature.read.generated.resources.custom_color_hint
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.read.presentation.model.ReadSelectionUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * Everything the user can do with the verses they picked. It is deliberately not modal: the reader
 * stays tappable underneath so the selection can be extended while the panel is open.
 */
@Composable
internal fun SelectionPanel(
    selection: ReadSelectionUiModel,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = verseReferenceLabel(
                    bookId = selection.bookId,
                    chapterNumber = selection.chapterNumber,
                    verseNumbers = selection.verseNumbers,
                ),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
            CommonIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.close),
                onClick = { onEvent(ReadUiEvent.OnClearSelectionClick) },
            )
        }
        HorizontalDivider()
        HighlightPaletteRow(
            customColors = selection.customColors,
            activeColor = selection.activeColor,
            onColorClick = { color -> onEvent(ReadUiEvent.OnHighlightColorClick(color)) },
            onCustomColorLongClick = { color -> onEvent(ReadUiEvent.OnCustomColorLongClick(color)) },
            onCustomColorPickerOpen = { onEvent(ReadUiEvent.OnCustomColorPickerOpen) },
        )
        if (selection.customColors.isNotEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.custom_color_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        selection.customColorPicker?.let { picker ->
            CustomColorPicker(
                color = picker,
                onColorChange = { hue, lightness ->
                    onEvent(
                        ReadUiEvent.OnCustomColorChange(
                            hue = hue,
                            lightness = lightness,
                        ),
                    )
                },
                onCancel = { onEvent(ReadUiEvent.OnCustomColorCancelClick) },
                onApply = { onEvent(ReadUiEvent.OnCustomColorApplyClick) },
            )
        }
        SelectionActionsRow(
            isSelectionSaved = selection.isSelectionSaved,
            onSaveClick = { onEvent(ReadUiEvent.OnToggleSavedClick) },
            onNoteClick = { onEvent(ReadUiEvent.OnNoteClick) },
            onShareClick = { onEvent(ReadUiEvent.OnShareClick) },
            onCopyClick = { onEvent(ReadUiEvent.OnCopyClick) },
        )
    }
}
