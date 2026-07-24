package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Keeps supabase-kt's desktop OAuth success page (`redirectHtml`) in sync with the user's
 * in-app theme and language preferences while a sign-in attempt is in progress.
 *
 * Usage: wrap any code that performs OAuth in [withSyncedRedirectHtml]. The success
 * [block] is only invoked once the FIRST render has been applied — if rendering fails,
 * [onError] is called instead and the block never runs. Subsequent rendering failures
 * during the OAuth flow are reported through [onError] too, but do not abort the block.
 */
internal class DesktopAuthRedirectHtmlSynchronizer(
    private val auth: Auth,
    private val getDesktopAuthSuccessHtmlFlow: GetDesktopAuthSuccessHtmlFlow,
) {
    suspend fun withSyncedRedirectHtml(
        onError: (Throwable) -> Unit,
        block: suspend () -> Unit,
    ) {
        coroutineScope {
            val firstResult = CompletableDeferred<Result<Unit>>()
            val watcher = getDesktopAuthSuccessHtmlFlow()
                .onEach { result ->
                    result
                        .onSuccess { html ->
                            auth.config.httpCallbackConfig.redirectHtml = html
                            firstResult.complete(Result.success(Unit))
                        }.onFailure { throwable ->
                            onError(throwable)
                            firstResult.complete(Result.failure(throwable))
                        }
                }.launchIn(this)

            // Make sure the initial render is in place BEFORE block() opens the OAuth tab;
            // otherwise a fast callback could race the watcher and the user would briefly see
            // the default supabase-kt page.
            val firstOutcome = firstResult.await()

            try {
                firstOutcome.onSuccess { block() }
            } finally {
                watcher.cancel()
            }
        }
    }
}
