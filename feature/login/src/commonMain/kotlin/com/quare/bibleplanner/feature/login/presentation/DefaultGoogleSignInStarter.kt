package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/**
 * Default starter that delegates to [NativeSignInState.startFlow], used on platforms where
 * `compose-auth` already provides a native sign-in experience (Android, iOS).
 *
 * Desktop / JVM uses [JvmGoogleSignInStarter] instead, because `compose-auth` on JVM falls
 * back to the Supabase OAuth flow and ends up redirecting to the project's Site URL instead
 * of returning the session to the desktop app.
 */
internal class DefaultGoogleSignInStarter : GoogleSignInStarter {
    override suspend fun invoke(nativeSignInState: NativeSignInState): Result<Unit> =
        runCatching { nativeSignInState.startFlow() }
}
