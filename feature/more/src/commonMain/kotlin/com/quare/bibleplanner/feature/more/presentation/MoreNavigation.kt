package com.quare.bibleplanner.feature.more.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.feature.more.presentation.utils.MoreUiActionCollector
import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.more(
    navController: NavController,
    mainScaffoldState: MainScaffoldState,
) {
    composable<BottomNavRoute.More> {
        val viewModel = koinViewModel<MoreViewModel>()
        MoreUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
            snackbarHostState = mainScaffoldState.snackbarHostState,
        )
        val uiState by viewModel.uiState.collectAsState()
        MoreScreen(
            items = uiState.items,
            onEvent = viewModel::onEvent,
        )
    }
}
