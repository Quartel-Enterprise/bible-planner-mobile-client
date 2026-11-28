package com.quare.bibleplanner.feature.deleteaccount.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_error_message
import bibleplanner.feature.delete_account.generated.resources.delete_account_success_message
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

@Composable
internal fun DeleteAccountUiActionCollector(uiActionFlow: Flow<DeleteAccountUiAction>) {
    val snackbarHostState = LocalSnackbarHostState.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            DeleteAccountUiAction.ShowError -> snackbarHostState.showSnackbar(
                getString(Res.string.delete_account_error_message),
                withDismissAction = true,
            )

            DeleteAccountUiAction.NotifySuccess -> appSnackbarController.show(
                AppSnackbarMessage.Resource(
                    stringResource = Res.string.delete_account_success_message,
                    isDismissible = false,
                ),
            )
        }
    }
}
