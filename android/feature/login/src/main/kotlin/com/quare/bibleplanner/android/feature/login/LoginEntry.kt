package com.quare.bibleplanner.android.feature.login

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.LoginViewModel
import com.quare.bibleplanner.feature.login.presentation.model.LoginResultMessage
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import io.github.jan.supabase.compose.auth.composable.GoogleDialogType
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun LoginNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<LoginViewModel>()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is LoginUiAction.NotifyLoginResult -> if (notifyResultViaSnackbar) {
                appSnackbarController.show(context.createResultMessage(uiAction.message))
            }
        }
    }
    val composeAuth = viewModel.composeAuth
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.GOOGLE,
                    result = result,
                ),
            )
        },
        type = GoogleDialogType.BOTTOM_SHEET,
    )
    val appleSignInState = composeAuth.rememberSignInWithApple(
        onResult = { result ->
            viewModel.onEvent(
                LoginUiEvent.SocialAuthResult(
                    provider = LoginProvider.APPLE,
                    result = result,
                ),
            )
        },
    )
    ExpressiveTheme {
        LoginContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onProviderClick = { provider ->
                /*
                 * Why: the ViewModel marks the provider as loading as soon as it gets the click, while the
                 * collected state only disables its button a frame later, so a double tap would start sign-in twice.
                 */
                val isAlreadyStarting = viewModel.state.value.loadingProvider == provider
                if (!isAlreadyStarting) {
                    viewModel.onEvent(
                        LoginUiEvent.SocialLoginClick(
                            provider = provider,
                            nativeSignInState = when (provider) {
                                LoginProvider.GOOGLE -> googleSignInState
                                LoginProvider.APPLE -> appleSignInState
                            },
                        ),
                    )
                }
            },
        )
    }
}

private fun Context.createResultMessage(message: LoginResultMessage): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(message.toText()),
    isDismissible = false,
)

@StringRes
private fun LoginResultMessage.toText(): Int = when (this) {
    LoginResultMessage.SUCCESS -> R.string.login_result_success
    LoginResultMessage.ERROR -> R.string.login_result_error
}
