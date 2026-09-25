package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.add_notes_free_warning.generated.resources.Res
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_message
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_title
import bibleplanner.feature.add_notes_free_warning.generated.resources.cancel
import bibleplanner.feature.add_notes_free_warning.generated.resources.see_plans
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddNotesFreeWarningDialog(
    maxFreeNotesAmount: Int,
    onEvent: (AddNotesFreeWarningUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.add_notes_free_warning_title),
        text = stringResource(
            Res.string.add_notes_free_warning_message,
            maxFreeNotesAmount,
        ),
        confirmText = stringResource(Res.string.see_plans),
        onConfirm = { onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPro) },
        dismissText = stringResource(Res.string.cancel),
        onDismiss = { onEvent(AddNotesFreeWarningUiEvent.OnCancel) },
    )
}
