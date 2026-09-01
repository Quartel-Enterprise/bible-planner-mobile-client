package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_title
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.route.EditPhotoSourceNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.content.PhotoSourceSheetContent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.editPhotoSource() {
    entry<EditPhotoSourceNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<ProfilePhotoViewModel>()
        val navigator = koinInject<Navigator>()
        val uiState by viewModel.uiState.collectAsState()
        ResponsiveDialogSheet(
            onCloseClick = navigator::navigateBack,
            title = stringResource(Res.string.edit_profile_photo_title),
        ) {
            PhotoSourceSheetContent(
                profile = uiState.profile.valueOrNull(),
                isCameraAvailable = uiState.isCameraAvailable,
                onEvent = viewModel::onEvent,
            )
        }
        ProfilePhotoPickers(
            viewModel = viewModel,
            onOpenCrop = navigator::navigateReplacingTop,
            onPhotoChanged = navigator::navigateBack,
        )
    }
}
