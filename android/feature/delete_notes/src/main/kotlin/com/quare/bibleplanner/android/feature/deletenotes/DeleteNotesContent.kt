package com.quare.bibleplanner.android.feature.deletenotes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.deletenotes.presentation.model.DeleteNotesUiEvent

@Composable
internal fun DeleteNotesContent(onEvent: (DeleteNotesUiEvent) -> Unit) {
    AppAlertDialog(
        title = stringResource(R.string.delete_notes_title),
        text = stringResource(R.string.delete_notes_message),
        confirmText = stringResource(R.string.delete_notes_confirm),
        onConfirm = { onEvent(DeleteNotesUiEvent.OnConfirmDelete) },
        dismissText = stringResource(R.string.delete_notes_cancel),
        onDismiss = { onEvent(DeleteNotesUiEvent.OnCancel) },
        isDestructive = true,
    )
}
