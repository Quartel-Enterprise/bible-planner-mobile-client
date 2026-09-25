package com.quare.bibleplanner.feature.deleteversion.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.delete_version.generated.resources.Res
import bibleplanner.feature.delete_version.generated.resources.cancel
import bibleplanner.feature.delete_version.generated.resources.delete
import bibleplanner.feature.delete_version.generated.resources.delete_version_message
import bibleplanner.feature.delete_version.generated.resources.delete_version_title
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import com.quare.bibleplanner.ui.component.dialog.AppProgressDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteVersionScreen(
    uiState: DeleteVersionUiState,
    onEvent: (DeleteVersionUiEvent) -> Unit,
) {
    when (uiState) {
        DeleteVersionUiState.Idle -> AppAlertDialog(
            title = stringResource(Res.string.delete_version_title),
            text = stringResource(Res.string.delete_version_message),
            confirmText = stringResource(Res.string.delete),
            onConfirm = { onEvent(DeleteVersionUiEvent.OnConfirmDelete) },
            dismissText = stringResource(Res.string.cancel),
            onDismiss = { onEvent(DeleteVersionUiEvent.OnCancel) },
            isDestructive = true,
        )

        DeleteVersionUiState.Loading -> AppProgressDialog(message = null)
    }
}
