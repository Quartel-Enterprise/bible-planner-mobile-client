package com.quare.bibleplanner.feature.login.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource

@Composable
internal fun LoginUiActionCollector(
    uiActionFlow: Flow<LoginUiAction>,
    onLoginResult: (StringResource) -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            is LoginUiAction.NotifyLoginResult -> onLoginResult(uiAction.message)
        }
    }
}
