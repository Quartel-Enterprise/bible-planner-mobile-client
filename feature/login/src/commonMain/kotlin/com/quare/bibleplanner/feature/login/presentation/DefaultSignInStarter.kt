package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/*
 * Why: Android/iOS only; compose-auth on JVM falls back to OAuth and redirects to the
 * project's Site URL instead of the app, so desktop uses JvmSignInStarter. Android has
 * no native Apple sign-in and falls back to OAuth in a Custom Tab.
 */
internal class DefaultSignInStarter : SignInStarter {
    override suspend fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit> = suspendRunCatching { nativeSignInState.startFlow() }
}
