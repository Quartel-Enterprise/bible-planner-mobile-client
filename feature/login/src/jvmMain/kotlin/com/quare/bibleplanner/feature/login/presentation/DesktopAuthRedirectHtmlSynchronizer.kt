package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

            /*
             * Why: the first render must be installed before block() opens the OAuth tab, or a fast callback
             * races the watcher and shows the default supabase-kt page.
             */
            val firstOutcome = firstResult.await()

            try {
                firstOutcome.onSuccess { block() }
            } finally {
                watcher.cancel()
            }
        }
    }
}
