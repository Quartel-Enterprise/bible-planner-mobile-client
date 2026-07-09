package com.quare.bibleplanner.feature.logout.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

@Composable
internal fun LogoutUiActionCollector(
    uiActionFlow: Flow<LogoutUiAction>,
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            LogoutUiAction.NavigateBack -> onNavigateBack()

            is LogoutUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message),
                withDismissAction = true,
            )

            is LogoutUiAction.NotifySuccess -> appSnackbarController.show(
                AppSnackbarMessage(
                    stringResource = action.message,
                    isDismissible = false,
                ),
            )
        }
    }
}
