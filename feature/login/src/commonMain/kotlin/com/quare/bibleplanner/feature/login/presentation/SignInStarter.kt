package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

// Why: the Result only reports whether the flow started; the sign-in outcome arrives via
// NativeSignInResult (Android/iOS) or supabase sessionStatus (desktop, GoTrue OAuth fallback).
fun interface SignInStarter {
    suspend operator fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit>
}
