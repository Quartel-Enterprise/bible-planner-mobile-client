package com.quare.bibleplanner.feature.daystudy.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.dayStudyDetailPane
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyRouteViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.dayStudy(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<DayStudyNavRoute>(metadata = dayStudyDetailPane()) { route ->
        DayStudyRootContent(
            route = route,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
    }
}

@Composable
private fun DayStudyRootContent(
    route: DayStudyNavRoute,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<DayStudyRouteViewModel> { parametersOf(route) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isWide = LocalIsWideLayout.current

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is DayStudyRouteUiAction.NavigateToRoute -> onNavigate(action.route)

            is DayStudyRouteUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(getString(action.message))

            is DayStudyRouteUiAction.ShowSnackBarPlural ->
                snackbarHostState.showSnackbar(getPluralString(action.resource, action.count, action.count))
        }
    }

    DayStudyScreen(
        uiState = uiState,
        isWide = isWide,
        snackbarHostState = snackbarHostState,
        onCardClick = { viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick) },
        onNavigateBack = onNavigateBack,
    )
}
