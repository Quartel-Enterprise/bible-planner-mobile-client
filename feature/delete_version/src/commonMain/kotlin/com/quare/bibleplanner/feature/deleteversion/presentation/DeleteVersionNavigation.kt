package com.quare.bibleplanner.feature.deleteversion.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel.DeleteVersionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.deleteVersion(navController: NavController) {
    dialog<DeleteVersionNavRoute> {
        val viewModel: DeleteVersionViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        ActionCollector(viewModel.backUiAction) {
            navController.navigateUp()
        }

        DeleteVersionScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
