package com.quare.bibleplanner.feature.login.presentation.model

import io.github.jan.supabase.compose.auth.composable.NativeSignInState

sealed interface LoginUiEvent {
    data object DismissClick : LoginUiEvent

    data object NotNowClick : LoginUiEvent

    sealed interface SocialLoginClick : LoginUiEvent {
        val nativeSignInState: NativeSignInState

        data class Google(
            override val nativeSignInState: NativeSignInState,
        ) : SocialLoginClick

        data class Apple(
            override val nativeSignInState: NativeSignInState,
        ) : SocialLoginClick
    }
}
