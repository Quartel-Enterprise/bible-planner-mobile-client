package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import kotlinx.coroutines.CancellationException

internal class JvmAppleSignInStarter(
    private val supabaseClient: SupabaseClient,
    private val redirectHtmlSynchronizer: DesktopAuthRedirectHtmlSynchronizer,
) : AppleSignInStarter {
    override suspend fun invoke(nativeSignInState: NativeSignInState): Result<Unit> {
        var capturedError: Throwable? = null
        redirectHtmlSynchronizer.withSyncedRedirectHtml(
            onError = { capturedError = it },
        ) {
            try {
                supabaseClient.auth.signInWith(Apple)
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
}
