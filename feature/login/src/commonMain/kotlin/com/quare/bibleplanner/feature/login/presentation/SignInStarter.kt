package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

fun interface SignInStarter {
    suspend operator fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit>
}
