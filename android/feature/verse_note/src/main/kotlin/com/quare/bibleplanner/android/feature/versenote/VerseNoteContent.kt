package com.quare.bibleplanner.android.feature.versenote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.android.designsystem.text.rememberSyncedTextFieldState
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiState
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import com.quare.bibleplanner.ui.utils.sheet.SheetCloseGuard

private const val QUOTE_MAX_LINES = 3
private val quoteBorderWidth = 3.dp
private val fieldShape = RoundedCornerShape(12.dp)

@Composable
internal fun VerseNoteContent(
    uiState: VerseNoteUiState,
    onEvent: (VerseNoteUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(if (uiState.isExisting) R.string.verse_note_edit_title else R.string.verse_note_title),
        onDismiss = { onEvent(VerseNoteUiEvent.OnDismiss) },
    ) { sheetCloseGuard ->
        VerseNoteEditorContent(
            uiState = uiState,
            sheetCloseGuard = sheetCloseGuard,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun VerseNoteEditorContent(
    uiState: VerseNoteUiState,
    sheetCloseGuard: SheetCloseGuard,
    onEvent: (VerseNoteUiEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    val synced = rememberSyncedTextFieldState(
        value = uiState.text,
        onValueChange = { text -> onEvent(VerseNoteUiEvent.OnTextChange(text)) },
    )
    val reference = verseReferenceLabel(
        bookId = uiState.bookId,
        chapterNumber = uiState.chapterNumber,
        verseNumbers = uiState.verseNumbers,
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = reference,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        if (uiState.quote.isNotEmpty()) {
            VerseNoteQuoteCard(quote = uiState.quote)
        }
        OutlinedTextField(
            state = synced.textFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .focusRequester(focusRequester),
            placeholder = { Text(text = stringResource(R.string.verse_note_placeholder)) },
            shape = fieldShape,
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            // Why: the typed text reaches the view model a frame later, so the field decides; the click sends it.
            enabled = synced.textFieldState.text.isNotBlank(),
            onClick = {
                sheetCloseGuard.close {
                    synced.sendLatest()
                    onEvent(VerseNoteUiEvent.OnSaveClick)
                }
            },
        ) {
            Text(text = stringResource(R.string.verse_note_save))
        }
        if (uiState.isExisting) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(VerseNoteUiEvent.OnDeleteClick) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.verse_note_delete))
            }
        }
    }
    if (uiState.isDeleteConfirmationVisible) {
        DeleteVerseNoteDialog(
            reference = reference,
            onConfirm = { sheetCloseGuard.close { onEvent(VerseNoteUiEvent.OnDeleteConfirm) } },
            onDismiss = { onEvent(VerseNoteUiEvent.OnDeleteCancel) },
        )
    }
}

@Composable
private fun VerseNoteQuoteCard(quote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(fieldShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(
            modifier = Modifier
                .width(quoteBorderWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        SelectionContainer {
            Text(
                modifier = Modifier.padding(12.dp),
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = displaySerifFontFamily(),
                fontStyle = FontStyle.Italic,
                maxLines = QUOTE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
