package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.CropPhotoViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun CropPhotoNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    val viewModel = koinViewModel<CropPhotoViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    // Why: the ViewModel pops this screen before it emits the snackbar, so it goes to the root host.
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is CropPhotoUiAction.ShowSnackbar -> appSnackbarController.show(
                AppSnackbarMessage.Text(
                    text = context.getString(action.message.toStringRes()),
                    isDismissible = true,
                ),
            )
        }
    }
    ExpressiveTheme {
        CropPhotoContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
