package com.quare.bibleplanner.feature.deletenotes.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.delete_notes.generated.resources.Res
import bibleplanner.feature.delete_notes.generated.resources.cancel
import bibleplanner.feature.delete_notes.generated.resources.delete
import bibleplanner.feature.delete_notes.generated.resources.delete_notes_message
import bibleplanner.feature.delete_notes.generated.resources.delete_notes_title
import com.quare.bibleplanner.feature.deletenotes.presentation.model.DeleteNotesUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteNotesDialog(onEvent: (DeleteNotesUiEvent) -> Unit) {
    AppAlertDialog(
        title = stringResource(Res.string.delete_notes_title),
        text = stringResource(Res.string.delete_notes_message),
        confirmText = stringResource(Res.string.delete),
        onConfirm = { onEvent(DeleteNotesUiEvent.OnConfirmDelete) },
        dismissText = stringResource(Res.string.cancel),
        onDismiss = { onEvent(DeleteNotesUiEvent.OnCancel) },
        isDestructive = true,
    )
}
