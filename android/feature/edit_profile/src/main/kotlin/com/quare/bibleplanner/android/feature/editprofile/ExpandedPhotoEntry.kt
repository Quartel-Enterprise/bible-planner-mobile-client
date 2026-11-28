package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import software.quare.loadable.valueOrNull

internal fun ExpandedPhotoNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
) {
    val viewModel = koinViewModel<ProfilePhotoViewModel>()
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    var isDismissed by remember { mutableStateOf(false) }
    // Why: a double tap on the backdrop or the close button would otherwise also pop the profile screen below.
    val dismiss = {
        if (!isDismissed) {
            isDismissed = true
            navigator.navigateBack()
        }
    }
    ExpressiveTheme {
        ExpandedPhotoContent(
            profile = uiState.profile.valueOrNull(),
            isCameraAvailable = uiState.isCameraAvailable,
            onDismiss = dismiss,
            onEvent = viewModel::onEvent,
        )
        ProfilePhotoPickersComponent(
            viewModel = viewModel,
            onOpenCrop = navigator::navigate,
            onPhotoChanged = {},
        )
    }
}
