package com.quare.bibleplanner.android.feature.addnotesfreewarning

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent

@Composable
internal fun AddNotesFreeWarningContent(
    maxFreeNotesAmount: Int,
    type: AddNotesFreeWarningType,
    onEvent: (AddNotesFreeWarningUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.add_notes_free_warning_title),
        text = stringResource(
            type.toMessageResource(),
            maxFreeNotesAmount,
        ),
        confirmText = stringResource(R.string.add_notes_free_warning_see_plans),
        onConfirm = { onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPro) },
        dismissText = stringResource(R.string.add_notes_free_warning_cancel),
        onDismiss = { onEvent(AddNotesFreeWarningUiEvent.OnCancel) },
    )
}

@StringRes
private fun AddNotesFreeWarningType.toMessageResource(): Int = when (this) {
    AddNotesFreeWarningType.DAY -> R.string.add_notes_free_warning_day_message
    AddNotesFreeWarningType.VERSE -> R.string.add_notes_free_warning_verse_message
}
