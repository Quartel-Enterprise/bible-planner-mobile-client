package com.quare.bibleplanner.feature.verse.addnote.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import bibleplanner.feature.verse.add_note.generated.resources.Res
import bibleplanner.feature.verse.add_note.generated.resources.cancel_delete_verse_note
import bibleplanner.feature.verse.add_note.generated.resources.confirm_delete_verse_note
import bibleplanner.feature.verse.add_note.generated.resources.delete_verse_note_body
import bibleplanner.feature.verse.add_note.generated.resources.delete_verse_note_title
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteVerseNoteDialog(
    bookId: BookId,
    chapterNumber: Int,
    verseNumbers: List<Int>,
    onEvent: (VerseNoteUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(
            Res.string.delete_verse_note_title,
            verseReferenceLabel(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
            ),
        ),
        text = stringResource(Res.string.delete_verse_note_body),
        confirmText = stringResource(Res.string.confirm_delete_verse_note),
        onConfirm = { onEvent(VerseNoteUiEvent.OnDeleteConfirm) },
        dismissText = stringResource(Res.string.cancel_delete_verse_note),
        onDismiss = { onEvent(VerseNoteUiEvent.OnDeleteCancel) },
        isDestructive = true,
        icon = Icons.Default.Delete,
    )
}
