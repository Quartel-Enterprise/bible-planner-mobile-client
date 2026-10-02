package com.quare.bibleplanner.core.daystudy.data.datasource

import com.quare.bibleplanner.core.daystudy.data.exception.StudyStreamStalledException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.functions.FunctionServerSentEvent
import io.github.jan.supabase.functions.Functions
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StudyFunctionClient(
    private val functions: Functions,
) {
    private val retryDelay: Duration = 1.seconds
    private val streamIdleTimeout: Duration = 90.seconds

    fun stream(
        functionName: String,
        body: String,
    ): Flow<FunctionServerSentEvent> {
        var receivedEvent = false
        return functions
            .invokeSSE(functionName) {
                contentType(ContentType.Application.Json)
                setBody(body)
                timeout {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
                }
            }.timeout(streamIdleTimeout)
            .catch { throwable ->
                if (throwable is TimeoutCancellationException) {
                    throw StudyStreamStalledException()
                } else {
                    throw throwable
                }
            }.onEach { receivedEvent = true }
            .retryWhen { cause, attempt ->
                val shouldRetry = attempt == 0L && !receivedEvent && cause.isTransientConnectionFailure()
                if (shouldRetry) delay(retryDelay)
                shouldRetry
            }
    }

    suspend fun fetch(
        functionName: String,
        body: String,
    ): String = functions
        .invoke(functionName) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyAsText()

    private fun Throwable.isTransientConnectionFailure(): Boolean =
        this is HttpRequestException || (this is SSEClientException && response == null)

    private companion object {
        const val REQUEST_TIMEOUT_MILLIS = 240_000L
        const val SOCKET_TIMEOUT_MILLIS = 60_000L
    }
}
