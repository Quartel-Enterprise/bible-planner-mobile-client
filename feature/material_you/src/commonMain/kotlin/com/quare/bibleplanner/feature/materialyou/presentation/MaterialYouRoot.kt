package com.quare.bibleplanner.feature.materialyou.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.feature.materialyou.presentation.component.MaterialYouDialog
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiAction
import com.quare.bibleplanner.feature.materialyou.presentation.viewmodel.AndroidColorSchemeViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.materialYou(
    navController: NavHostController,
) {
    dialog<MaterialYouBottomSheetNavRoute> {
        val viewModel = koinViewModel<AndroidColorSchemeViewModel>()
        val state by viewModel.uiState.collectAsState()
        MaterialYouActionCollector(
            navHostController = navController,
            flow = viewModel.uiAction
        )
        MaterialYouDialog(
            isMaterialYouActivated = state,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun MaterialYouActionCollector(
    flow: Flow<AndroidColorSchemeUiAction>,
    navHostController: NavHostController
) {
    ActionCollector(flow) { uiAction ->
        when (uiAction) {
            AndroidColorSchemeUiAction.CloseBottomSheet -> {
                navHostController.navigateUp()
            }
        }
    }
}
