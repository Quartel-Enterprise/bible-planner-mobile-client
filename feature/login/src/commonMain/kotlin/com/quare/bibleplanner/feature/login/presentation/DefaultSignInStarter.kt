package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

internal class DefaultSignInStarter : SignInStarter {
    override suspend fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit> = suspendRunCatching { nativeSignInState.startFlow() }
}
