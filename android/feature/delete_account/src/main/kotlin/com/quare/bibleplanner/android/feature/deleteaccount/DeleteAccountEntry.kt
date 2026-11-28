package com.quare.bibleplanner.android.feature.deleteaccount

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiAction
import com.quare.bibleplanner.feature.deleteaccount.presentation.viewmodel.DeleteAccountViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun DeleteAccountNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(
        DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ),
) {
    val viewModel = koinViewModel<DeleteAccountViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        appSnackbarController.show(context.toSnackbarMessage(uiAction))
    }
    ExpressiveTheme {
        DeleteAccountContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.toSnackbarMessage(uiAction: DeleteAccountUiAction): AppSnackbarMessage = when (uiAction) {
    DeleteAccountUiAction.ShowError -> AppSnackbarMessage.Text(
        text = getString(R.string.delete_account_error_message),
        isDismissible = true,
    )

    DeleteAccountUiAction.NotifySuccess -> AppSnackbarMessage.Text(
        text = getString(R.string.delete_account_success_message),
        isDismissible = false,
    )
}
