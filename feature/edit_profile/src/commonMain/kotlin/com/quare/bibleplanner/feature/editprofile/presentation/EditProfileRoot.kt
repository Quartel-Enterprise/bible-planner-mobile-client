package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_title
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.content.EditProfileSheetContent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditProfileViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.editProfile(
    onNavigateReplacingTop: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<EditProfileNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<EditProfileViewModel>()
        ActionCollector(viewModel.uiAction) { action ->
            onNavigateReplacingTop(action.route)
        }
        ResponsiveDialogSheet(
            onCloseClick = onNavigateBack,
            title = stringResource(Res.string.edit_profile_title),
        ) {
            EditProfileSheetContent(onEvent = viewModel::onEvent)
        }
    }
}
