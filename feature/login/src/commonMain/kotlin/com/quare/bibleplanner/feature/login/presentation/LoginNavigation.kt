package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.utils.LoginUiActionCollector
import com.quare.bibleplanner.ui.utils.ActionCollector
import io.github.jan.supabase.compose.auth.composable.GoogleDialogType
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.loginRoot(navController: NavController) {
    dialog<LoginNavRoute> {
        val viewModel = koinViewModel<LoginViewModel>()
        val onEvent = viewModel::onEvent
        val composeAuth = viewModel.composeAuth
        val googleAuthState = composeAuth.rememberSignInWithGoogle(
            type = GoogleDialogType.BOTTOM_SHEET,
        )
        val appleAuthState = composeAuth.rememberSignInWithApple(onResult = {})
        val sheetState = rememberModalBottomSheetState()
        val state by viewModel.state.collectAsState()
        LoginUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
            sheetState = sheetState,
        )
        LoginBottomSheet(
            sheetState = sheetState,
            onEvent = onEvent,
            onLoginWithGoogleClick = {
                onEvent(LoginUiEvent.SocialLoginClick.Google(googleAuthState))
            },
            onLoginWithAppleClick = {
                onEvent(LoginUiEvent.SocialLoginClick.Apple(appleAuthState))
            },
            enableProviders = state.enabledProviders,
        )
    }
}
