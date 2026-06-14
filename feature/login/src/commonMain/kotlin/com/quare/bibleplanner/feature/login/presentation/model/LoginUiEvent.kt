package com.quare.bibleplanner.feature.login.presentation.model

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

sealed interface LoginUiEvent {
    data object DismissClick : LoginUiEvent

    data object NotNowClick : LoginUiEvent

    data class SocialLoginClick(
        val provider: LoginProvider,
        val nativeSignInState: NativeSignInState,
    ) : LoginUiEvent

    data class SocialAuthResult(
        val provider: LoginProvider,
        val result: NativeSignInResult,
    ) : LoginUiEvent
}
