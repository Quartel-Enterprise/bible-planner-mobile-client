package com.quare.bibleplanner.feature.login.presentation.utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginUiActionCollector(
    uiActionFlow: Flow<LoginUiAction>,
    navController: NavController,
    sheetState: SheetState,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            LoginUiAction.NavigateBack -> navController.navigateUp()
            LoginUiAction.CloseBottomSheet -> sheetState.hide()
        }
    }
}
