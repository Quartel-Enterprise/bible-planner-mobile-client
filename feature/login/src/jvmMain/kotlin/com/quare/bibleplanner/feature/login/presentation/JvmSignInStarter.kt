package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

internal class JvmSignInStarter(
    private val supabaseClient: SupabaseClient,
    private val redirectHtmlSynchronizer: DesktopAuthRedirectHtmlSynchronizer,
) : SignInStarter {
    override suspend fun invoke(
        provider: LoginProvider,
        nativeSignInState: NativeSignInState,
    ): Result<Unit> {
        var capturedError: Throwable? = null
        redirectHtmlSynchronizer.withSyncedRedirectHtml(
            onError = { capturedError = it },
        ) {
            suspendRunCatching { signInWith(provider) }
                .onFailure { throwable -> capturedError = throwable }
        }
        return capturedError
            ?.let { Result.failure(it) }
            ?: Result.success(Unit)
    }

    private suspend fun signInWith(provider: LoginProvider) {
        when (provider) {
            LoginProvider.GOOGLE -> supabaseClient.auth.signInWith(Google) {
                scopes.add("email")
                scopes.add("profile")
            }

            LoginProvider.APPLE -> supabaseClient.auth.signInWith(Apple)
        }
    }
}
