package com.quare.bibleplanner.android.feature.daystudy

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.getDayStudyDetailPane
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyRouteViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun DayStudyNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    // Why: on wide screens the study panel scene draws this entry beside the day, the one with the main pane metadata.
    metadata = getDayStudyDetailPane(),
) {
    val viewModel = koinViewModel<DayStudyRouteViewModel> { parametersOf(this) }
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { action ->
        val message = when (action) {
            DayStudyRouteUiAction.ShowLimitReached -> context.getString(R.string.day_study_limit_reached)

            is DayStudyRouteUiAction.ShowWaitForGenerations -> context.resources.getQuantityString(
                R.plurals.day_study_wait_for_generations,
                action.count,
                action.count,
            )
        }
        snackbarHostState.showSnackbar(message)
    }
    ExpressiveTheme {
        DayStudyContent(
            state = uiState,
            isWide = LocalIsWideLayout.current,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
            onNavigateBack = navigator::navigateBack,
        )
    }
}
