package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.content.ExpandedPhotoOverlay
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.expandedPhoto(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<ExpandedPhotoNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<ProfilePhotoViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ProfilePhotoPickers(
            viewModel = viewModel,
            onOpenCrop = onNavigate,
            onPhotoChanged = {},
        )
        ExpandedPhotoOverlay(
            profile = uiState.profile.valueOrNull(),
            isCameraAvailable = uiState.isCameraAvailable,
            onDismiss = onNavigateBack,
            onEvent = viewModel::onEvent,
        )
    }
}
