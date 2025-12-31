package com.quare.bibleplanner.feature.day.presentation.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.nothing_to_delete_message
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun DayUiActionCollector(
    uiActionFlow: Flow<DayUiAction>,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is DayUiAction.ShowNothingToDeleteSnackbar -> {
                snackbarHostState.showSnackbar(
                    getString(Res.string.nothing_to_delete_message),
                )
            }

            is DayUiAction.NavigateBack -> {
                navController.navigateUp()
            }
        }
    }
}
