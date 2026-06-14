package com.quare.bibleplanner.feature.logout.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun LogoutUiActionCollector(
    uiActionFlow: Flow<LogoutUiAction>,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            LogoutUiAction.NavigateBack -> navController.navigateUp()
            is LogoutUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(getString(action.message))
        }
    }
}
