package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import kotlinx.browser.window

internal class WebSignInStarter(
    private val supabaseClient: SupabaseClient,
) : SignInStarter {
    override suspend fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit> = suspendRunCatching {
        val redirectUrl = window.location.origin
        when (provider) {
            LoginProvider.GOOGLE -> supabaseClient.auth.signInWith(
                provider = Google,
                redirectUrl = redirectUrl,
            )

            LoginProvider.APPLE -> supabaseClient.auth.signInWith(
                provider = Apple,
                redirectUrl = redirectUrl,
            )
        }
    }
}
