package com.quare.bibleplanner.android.feature.deleteversion

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.android.designsystem.dialog.AppProgressDialog
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState

@Composable
internal fun DeleteVersionContent(
    uiState: DeleteVersionUiState,
    onEvent: (DeleteVersionUiEvent) -> Unit,
) {
    when (uiState) {
        DeleteVersionUiState.Idle -> AppAlertDialog(
            title = stringResource(R.string.delete_version_title),
            text = stringResource(R.string.delete_version_message),
            confirmText = stringResource(R.string.delete_version_confirm),
            onConfirm = { onEvent(DeleteVersionUiEvent.OnConfirmDelete) },
            dismissText = stringResource(R.string.delete_version_cancel),
            onDismiss = { onEvent(DeleteVersionUiEvent.OnCancel) },
            isDestructive = true,
        )

        DeleteVersionUiState.Loading -> AppProgressDialog(message = null)
    }
}
