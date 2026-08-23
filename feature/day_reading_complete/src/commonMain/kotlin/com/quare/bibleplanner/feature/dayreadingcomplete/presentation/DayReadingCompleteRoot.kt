package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel.DayReadingCompleteViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.dayReadingComplete(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    entry<DayReadingCompleteNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        val viewModel = koinViewModel<DayReadingCompleteViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()

        DayReadingCompleteUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            onNavigateReplacingTop = onNavigateReplacingTop,
        )

        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(DayReadingCompleteUiEvent.OnDismiss) },
        ) {
            DayReadingCompleteSheet(
                uiState = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }
}

@Composable
private fun DayReadingCompleteUiActionCollector(
    uiActionFlow: Flow<DayReadingCompleteUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            DayReadingCompleteUiAction.NavigateBack -> onNavigateBack()

            is DayReadingCompleteUiAction.NavigateToRoute -> {
                if (action.replace) {
                    onNavigateReplacingTop(action.route)
                } else {
                    onNavigate(action.route)
                }
            }

            is DayReadingCompleteUiAction.ShowSnackBar -> {
                appSnackbarController.show(
                    AppSnackbarMessage(
                        stringResource = action.message,
                        isDismissible = true,
                    ),
                )
            }
        }
    }
}
