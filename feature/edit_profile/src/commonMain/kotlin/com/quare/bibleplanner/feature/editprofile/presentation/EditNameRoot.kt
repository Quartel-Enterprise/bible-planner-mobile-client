package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.route.EditNameNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.content.EditNameDialog
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditNameViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.editName(onNavigateBack: () -> Unit) {
    entry<EditNameNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<EditNameViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = LocalSnackbarHostState.current
        ActionCollector(viewModel.uiAction) { action ->
            when (action) {
                EditNameUiAction.NavigateBack -> onNavigateBack()

                is EditNameUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    getString(action.message),
                    withDismissAction = true,
                )
            }
        }
        val currentName = uiState.currentName
        if (currentName is Loadable.Loaded) {
            EditNameDialog(
                initialName = currentName.value.orEmpty(),
                onDismiss = onNavigateBack,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
