package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.utils.AppLanguageActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.appLanguage(navController: NavHostController) {
    dialog<AppLanguageNavRoute> {
        val viewModel = koinViewModel<AppLanguageViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        AppLanguageActionCollector(
            actionsFlow = viewModel.uiAction,
            navController = navController,
        )
        val onEvent = viewModel::onEvent
        ModalBottomSheet(onDismissRequest = { onEvent(AppLanguageUiEvent.OnDismiss) }) {
            AppLanguageContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
