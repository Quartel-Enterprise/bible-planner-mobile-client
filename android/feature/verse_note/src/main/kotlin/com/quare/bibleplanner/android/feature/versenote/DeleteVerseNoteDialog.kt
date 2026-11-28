package com.quare.bibleplanner.android.feature.versenote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog

@Composable
internal fun DeleteVerseNoteDialog(
    reference: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.verse_note_delete_title, reference),
        text = stringResource(R.string.verse_note_delete_body),
        confirmText = stringResource(R.string.verse_note_delete_confirm),
        onConfirm = onConfirm,
        dismissText = stringResource(R.string.verse_note_delete_cancel),
        onDismiss = onDismiss,
        isDestructive = true,
        icon = Icons.Default.Delete,
    )
}
