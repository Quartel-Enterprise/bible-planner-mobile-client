package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.EditNameNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditNameViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import software.quare.loadable.loadedOrNull

internal fun EditNameNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    HideSceneDialogScrimEffect()
    val viewModel = koinViewModel<EditNameViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val navigator = koinInject<Navigator>()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            EditNameUiAction.NotifyNameUpdated -> appSnackbarController.show(
                AppSnackbarMessage.Text(
                    text = context.getString(R.string.edit_profile_name_updated),
                    isDismissible = true,
                ),
            )
        }
    }
    val currentName = uiState.currentName.loadedOrNull()
    if (currentName != null) {
        ExpressiveTheme {
            EditNameContent(
                initialName = currentName.value.orEmpty(),
                onDismiss = navigator::navigateBack,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
