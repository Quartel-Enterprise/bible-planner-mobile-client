package com.quare.bibleplanner.feature.congrats.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiAction
import com.quare.bibleplanner.feature.congrats.presentation.viewmodel.CongratsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.congrats(navController: NavController) {
    dialog<CongratsNavRoute> {
        val viewModel: CongratsViewModel = koinViewModel()
        val onEvent = viewModel::onEvent

        CongratsActionCollector(
            viewModel = viewModel,
            navController = navController,
        )

        CongratsBottomSheet(
            onEvent = onEvent,
        )
    }
}

@Composable
private fun CongratsActionCollector(
    viewModel: CongratsViewModel,
    navController: NavController,
) {
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            CongratsUiAction.Close -> navController.navigateUp()
        }
    }
}
