package com.quare.bibleplanner.feature.login.presentation.utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginUiActionCollector(
    uiActionFlow: Flow<LoginUiAction>,
    onNavigateBack: () -> Unit,
    sheetState: SheetState,
    onLoginResult: (StringResource) -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            LoginUiAction.NavigateBack -> onNavigateBack()
            LoginUiAction.CloseBottomSheet -> sheetState.hide()
            is LoginUiAction.NotifyLoginResult -> onLoginResult(uiAction.message)
        }
    }
}
