package com.quare.bibleplanner.feature.verse.addnote.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.add_note.generated.resources.Res
import bibleplanner.feature.verse.add_note.generated.resources.note_placeholder
import bibleplanner.feature.verse.add_note.generated.resources.save_note
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiState
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import org.jetbrains.compose.resources.stringResource

private const val QUOTE_MAX_LINES = 3
private val quoteBorderWidth = 3.dp

@Composable
internal fun VerseNoteContent(
    uiState: VerseNoteUiState,
    onEvent: (VerseNoteUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = verseReferenceLabel(
                bookId = uiState.bookId,
                chapterNumber = uiState.chapterNumber,
                verseNumbers = uiState.verseNumbers,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        if (uiState.quote.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .width(quoteBorderWidth)
                        .heightIn(min = 24.dp)
                        .background(MaterialTheme.colorScheme.primary),
                ) {}
                SelectionContainer {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = uiState.quote,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = displaySerifFontFamily(),
                        fontStyle = FontStyle.Italic,
                        maxLines = QUOTE_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .focusRequester(focusRequester)
                .focusable(),
            value = uiState.text,
            onValueChange = { text -> onEvent(VerseNoteUiEvent.OnTextChange(text)) },
            placeholder = { Text(text = stringResource(Res.string.note_placeholder)) },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isSaveEnabled,
            onClick = { onEvent(VerseNoteUiEvent.OnSaveClick) },
        ) {
            Text(text = stringResource(Res.string.save_note))
        }
    }
}
