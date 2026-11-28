package com.quare.bibleplanner.android.feature.logout

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutError
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.feature.logout.presentation.viewmodel.LogoutViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun LogoutNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(
        DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ),
) {
    val viewModel = koinViewModel<LogoutViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is LogoutUiAction.ShowError -> appSnackbarController.show(context.createErrorMessage(uiAction.error))
            LogoutUiAction.NotifySuccess -> appSnackbarController.show(context.createSuccessMessage())
        }
    }
    ExpressiveTheme {
        LogoutContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.createErrorMessage(error: LogoutError): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(
        when (error) {
            LogoutError.UNKNOWN -> R.string.logout_error_message
        },
    ),
    isDismissible = true,
)

private fun Context.createSuccessMessage(): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(R.string.logout_success_message),
    isDismissible = false,
)
