package com.quare.bibleplanner.feature.readingplan.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.ReadingPlanNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.readingPlan(navController: NavController) {
    composable<ReadingPlanNavRoute> {
        val viewModel = koinViewModel<ReadingPlanViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        var scrollToWeekNumber by remember { mutableIntStateOf(0) }

        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                is ReadingPlanUiAction.GoToDay -> {
                    navController.navigate(
                        DayNavRoute(
                            dayNumber = uiAction.dayNumber,
                            weekNumber = uiAction.weekNumber,
                            readingPlanType = uiAction.readingPlanType.name,
                        ),
                    )
                }

                is ReadingPlanUiAction.ScrollToWeek -> {
                    scrollToWeekNumber = uiAction.weekNumber
                }

                ReadingPlanUiAction.GoToDeleteAllProgress -> {
                    navController.navigate(DeleteAllProgressNavRoute)
                }

                ReadingPlanUiAction.GoToTheme -> {
                    navController.navigate(ThemeNavRoute)
                }
            }
        }
        ReadingPlanScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            scrollToWeekNumber = scrollToWeekNumber,
            onScrollToWeekCompleted = { scrollToWeekNumber = 0 },
        )
    }
}
