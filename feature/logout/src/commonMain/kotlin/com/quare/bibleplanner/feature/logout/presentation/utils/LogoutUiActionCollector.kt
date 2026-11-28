package com.quare.bibleplanner.feature.logout.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.logout.generated.resources.Res
import bibleplanner.feature.logout.generated.resources.logout_error_message
import bibleplanner.feature.logout.generated.resources.logout_success_message
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutError
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

@Composable
internal fun LogoutUiActionCollector(uiActionFlow: Flow<LogoutUiAction>) {
    val snackbarHostState = LocalSnackbarHostState.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is LogoutUiAction.ShowError -> snackbarHostState.showSnackbar(
                getString(action.error.toStringResource()),
                withDismissAction = true,
            )

            LogoutUiAction.NotifySuccess -> appSnackbarController.show(
                AppSnackbarMessage.Resource(
                    stringResource = Res.string.logout_success_message,
                    isDismissible = false,
                ),
            )
        }
    }
}

private fun LogoutError.toStringResource(): StringResource = when (this) {
    LogoutError.UNKNOWN -> Res.string.logout_error_message
}
