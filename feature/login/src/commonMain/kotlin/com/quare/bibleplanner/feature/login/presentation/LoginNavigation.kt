package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.utils.LoginUiActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import io.github.jan.supabase.compose.auth.composable.GoogleDialogType
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.loginRoot() {
    entry<LoginNavRoute>(metadata = getSheetPane()) { route ->
        val notifyResultViaSnackbar = route.notifyResultViaSnackbar
        val appSnackbarController = koinInject<AppSnackbarController>()
        val viewModel = koinViewModel<LoginViewModel>()
        val sheetCloseGuard = rememberSheetCloseGuard()
        val onEvent: (LoginUiEvent) -> Unit = { event ->
            if (event == LoginUiEvent.DismissClick || event == LoginUiEvent.NotNowClick) {
                sheetCloseGuard.close { viewModel.onEvent(event) }
            } else {
                viewModel.onEvent(event)
            }
        }
        val composeAuth = viewModel.composeAuth
        val nativeSignInStates = mapOf(
            LoginProvider.GOOGLE to composeAuth.rememberSignInWithGoogle(
                onResult = { result -> onEvent(LoginUiEvent.SocialAuthResult(LoginProvider.GOOGLE, result)) },
                type = GoogleDialogType.BOTTOM_SHEET,
            ),
            LoginProvider.APPLE to composeAuth.rememberSignInWithApple(
                onResult = { result -> onEvent(LoginUiEvent.SocialAuthResult(LoginProvider.APPLE, result)) },
            ),
        )
        val sheetState = rememberModalBottomSheetState()
        val state by viewModel.state.collectAsState()
        SheetExitAnimationEffect(
            animateOut = sheetState::hide,
            animateBackIn = sheetState::show,
        )
        LoginUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onLoginResult = { message ->
                if (notifyResultViaSnackbar) {
                    appSnackbarController.show(
                        AppSnackbarMessage(
                            stringResource = message,
                            isDismissible = false,
                        ),
                    )
                }
            },
        )
        LoginBottomSheet(
            sheetState = sheetState,
            isClosing = sheetCloseGuard.isClosing,
            onEvent = onEvent,
            onProviderClick = { provider ->
                nativeSignInStates[provider]?.let { nativeSignInState ->
                    onEvent(LoginUiEvent.SocialLoginClick(provider, nativeSignInState))
                }
            },
            enableProviders = state.enabledProviders,
            loadingProvider = state.loadingProvider,
            error = state.error,
        )
        if (state.showGoogleSignInUnavailableDialog) {
            GoogleSignInUnavailableDialog(onEvent = onEvent)
        }
    }
}
