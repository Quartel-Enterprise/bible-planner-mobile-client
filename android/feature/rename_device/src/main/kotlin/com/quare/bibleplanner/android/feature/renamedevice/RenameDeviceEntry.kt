package com.quare.bibleplanner.android.feature.renamedevice

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.RenameDeviceViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun RenameDeviceNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<RenameDeviceViewModel> { parametersOf(this) }
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            RenameDeviceUiAction.ShowError -> appSnackbarController.show(context.createErrorMessage())
        }
    }
    ExpressiveTheme {
        RenameDeviceContent(
            initialName = viewModel.currentName,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.createErrorMessage(): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(R.string.rename_device_error),
    isDismissible = true,
)
