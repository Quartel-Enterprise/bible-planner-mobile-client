package com.quare.bibleplanner.feature.accountdetails.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_action_error
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun RenameDeviceUiActionCollector(uiActionFlow: Flow<RenameDeviceUiAction>) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            RenameDeviceUiAction.NotifyRenameFailed -> appSnackbarController.show(
                AppSnackbarMessage(
                    stringResource = Res.string.account_details_action_error,
                    isDismissible = true,
                ),
            )
        }
    }
}
