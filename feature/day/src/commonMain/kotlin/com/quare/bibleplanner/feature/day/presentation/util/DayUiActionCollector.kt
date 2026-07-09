package com.quare.bibleplanner.feature.day.presentation.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun DayUiActionCollector(
    uiActionFlow: Flow<DayUiAction>,
    snackbarHostState: SnackbarHostState,
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is DayUiAction.ShowSnackBar -> {
                snackbarHostState.showSnackbar(getString(action.message))
            }

            is DayUiAction.ShowSnackBarText -> {
                snackbarHostState.showSnackbar(action.message)
            }

            is DayUiAction.NavigateBack -> {
                onNavigateBack()
            }

            is DayUiAction.NavigateToRoute -> {
                onNavigate(action.route)
            }

            DayUiAction.ClearFocus -> {
                focusManager.clearFocus()
            }
        }
    }
}
