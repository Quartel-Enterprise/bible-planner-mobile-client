package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.EditPhotoSourceNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import software.quare.loadable.valueOrNull

internal fun EditPhotoSourceNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ProfilePhotoViewModel>()
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        EditPhotoSourceContent(
            profile = uiState.profile.valueOrNull(),
            isCameraAvailable = uiState.isCameraAvailable,
            onDismiss = navigator::navigateBack,
            onEvent = viewModel::onEvent,
        )
        ProfilePhotoPickersComponent(
            viewModel = viewModel,
            onOpenCrop = navigator::navigateReplacingTop,
            onPhotoChanged = navigator::navigateBack,
        )
    }
}
