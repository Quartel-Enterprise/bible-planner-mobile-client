package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import kotlinx.coroutines.CancellationException

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
            try {
                signInWith(provider)
                // On success the supabase Auth plugin updates sessionStatus to
                // Authenticated, and LoginViewModel's observer closes the bottom sheet.
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                capturedError = throwable
            }
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
