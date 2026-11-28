package com.quare.bibleplanner.feature.login.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.login_result_error
import bibleplanner.feature.login.generated.resources.login_result_success
import com.quare.bibleplanner.feature.login.presentation.model.LoginResultMessage
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
            is LoginUiAction.NotifyLoginResult -> onLoginResult(uiAction.message.toStringResource())
        }
    }
}

private fun LoginResultMessage.toStringResource(): StringResource = when (this) {
    LoginResultMessage.SUCCESS -> Res.string.login_result_success
    LoginResultMessage.ERROR -> Res.string.login_result_error
}
