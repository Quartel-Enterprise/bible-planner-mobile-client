package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/**
 * Starts the Apple sign-in flow for the current platform and reports whether the
 * INITIATION succeeded as a [Result].
 *
 * Note: the actual sign-in OUTCOME (user authenticated, cancelled, network error, …) is
 * delivered separately through the `NativeSignInResult` callback wired into the
 * [NativeSignInState] for iOS, or through the supabase `sessionStatus` flow for Android/desktop
 * (which fall back to the GoTrue OAuth flow). [Result.failure] here means the flow itself could
 * not be started (e.g. the desktop success page failed to render, or `signInWith` threw before
 * the browser opened).
 */
fun interface AppleSignInStarter {
    suspend operator fun invoke(nativeSignInState: NativeSignInState): Result<Unit>
}
