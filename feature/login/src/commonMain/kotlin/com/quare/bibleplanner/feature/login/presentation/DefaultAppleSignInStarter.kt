package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/**
 * Default starter that delegates to [NativeSignInState.startFlow].
 *
 * On iOS this triggers the native Sign in with Apple sheet (configured via `appleNativeLogin()`).
 * On Android `compose-auth` has no native Apple support, so it transparently falls back to the
 * Supabase OAuth flow (a Custom Tab / browser).
 *
 * Desktop / JVM uses [JvmAppleSignInStarter] instead, because `compose-auth` on JVM falls back to
 * the Supabase OAuth flow and ends up redirecting to the project's Site URL instead of returning
 * the session to the desktop app.
 */
internal class DefaultAppleSignInStarter : AppleSignInStarter {
    override suspend fun invoke(nativeSignInState: NativeSignInState): Result<Unit> =
        runCatching { nativeSignInState.startFlow() }
}
