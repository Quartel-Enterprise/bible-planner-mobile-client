package com.quare.bibleplanner.feature.daystudy.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.getDayStudyDetailPane
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyRouteViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.dayStudy() {
    entry<DayStudyNavRoute>(metadata = getDayStudyDetailPane()) { route ->
        DayStudyRootContent(route = route)
    }
}

@Composable
private fun DayStudyRootContent(route: DayStudyNavRoute) {
    val viewModel = koinViewModel<DayStudyRouteViewModel> { parametersOf(route) }
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isWide = LocalIsWideLayout.current

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
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
        onRetryClick = { viewModel.onEvent(DayStudyRouteUiEvent.OnRetryClick) },
        onAskAiClick = { viewModel.onEvent(DayStudyRouteUiEvent.OnAskAiClick) },
        onNavigateBack = navigator::navigateBack,
    )
}
