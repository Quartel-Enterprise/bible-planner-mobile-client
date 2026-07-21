package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.content.CropPhotoScreen
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.CropPhotoViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.cropPhoto(onNavigateBack: () -> Unit) {
    entry<CropPhotoNavRoute> { route ->
        val viewModel = koinViewModel<CropPhotoViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = LocalSnackbarHostState.current
        ActionCollector(viewModel.uiAction) { action ->
            when (action) {
                CropPhotoUiAction.NavigateBack -> onNavigateBack()

                is CropPhotoUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    getString(action.message),
                    withDismissAction = true,
                )
            }
        }
        CropPhotoScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
