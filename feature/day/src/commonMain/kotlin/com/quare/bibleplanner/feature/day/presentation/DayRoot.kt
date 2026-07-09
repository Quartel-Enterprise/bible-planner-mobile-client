package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.component.TimeEditionDialog
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.util.DayUiActionCollector
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.day(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<DayNavRoute> { backStackEntry ->
        DayRootContent(
            route = backStackEntry.toRoute(),
            onNavigate = { route -> navController.navigate(route) },
            onNavigateBack = { navController.navigateUp() },
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayRootContent(
    route: DayNavRoute,
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel = koinViewModel<DayViewModel> { parametersOf(route) }
    val uiState by viewModel.uiState.collectAsState()
    val onEvent = viewModel::onEvent
    val snackbarHostState = remember { SnackbarHostState() }

    DayUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
    (uiState as? DayUiState.Loaded)?.run {
        datePickerUiState.visiblePicker?.let {
            TimeEditionDialog(
                type = it,
                onEvent = onEvent,
                datePickerUiState = datePickerUiState,
            )
        }
    }
    DayScreen(
        platform = viewModel.platform,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
    )
}
