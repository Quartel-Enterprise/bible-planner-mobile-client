package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.add_notes_free_warning.generated.resources.Res
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_message
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_title
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_verse_notes_free_warning_message
import bibleplanner.feature.add_notes_free_warning.generated.resources.cancel
import bibleplanner.feature.add_notes_free_warning.generated.resources.see_plans
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddNotesFreeWarningDialog(
    maxFreeNotesAmount: Int,
    type: AddNotesFreeWarningType,
    onEvent: (AddNotesFreeWarningUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.add_notes_free_warning_title),
        text = stringResource(
            type.toMessageResource(),
            maxFreeNotesAmount,
        ),
        confirmText = stringResource(Res.string.see_plans),
        onConfirm = { onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPro) },
        dismissText = stringResource(Res.string.cancel),
        onDismiss = { onEvent(AddNotesFreeWarningUiEvent.OnCancel) },
    )
}

private fun AddNotesFreeWarningType.toMessageResource(): StringResource = when (this) {
    AddNotesFreeWarningType.DAY -> Res.string.add_notes_free_warning_message
    AddNotesFreeWarningType.VERSE -> Res.string.add_verse_notes_free_warning_message
}
