package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/**
 * Default starter that delegates to [NativeSignInState.startFlow], used on platforms where
 * `compose-auth` already provides a native sign-in experience (Android, iOS).
 *
 * On iOS this triggers the native sheet for each provider (configured via `googleNativeLogin()`
 * / `appleNativeLogin()`). On Android `compose-auth` has no native Apple support, so it
 * transparently falls back to the Supabase OAuth flow (a Custom Tab / browser).
 *
 * Desktop / JVM uses [JvmSignInStarter] instead, because `compose-auth` on JVM falls back to the
 * Supabase OAuth flow and ends up redirecting to the project's Site URL instead of returning the
 * session to the desktop app.
 */
internal class DefaultSignInStarter : SignInStarter {
    override suspend fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit> = suspendRunCatching { nativeSignInState.startFlow() }
}
