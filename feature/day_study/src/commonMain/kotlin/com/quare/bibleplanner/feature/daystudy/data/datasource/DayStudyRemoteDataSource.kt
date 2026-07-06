package com.quare.bibleplanner.feature.daystudy.data.datasource

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyProgressDto
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.feature.daystudy.data.model.DayStudyStreamEvent
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.functions.FunctionServerSentEvent
import io.github.jan.supabase.functions.Functions
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class DayStudyRemoteDataSource(
    private val functions: Functions,
    private val json: Json,
) {
    private val requestJson = Json { explicitNulls = false }
    private val retryDelay: Duration = 1.seconds

    fun streamDayStudy(request: DayStudyRequestDto): Flow<DayStudyStreamEvent> {
        val body = requestJson.encodeToString(DayStudyRequestDto.serializer(), request)
        var receivedEvent = false
        return functions
            .invokeSSE(FUNCTION_NAME) {
                contentType(ContentType.Application.Json)
                setBody(body)
                timeout {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
                }
            }.onEach { receivedEvent = true }
            .mapNotNull(::mapEvent)
            .retryWhen { cause, attempt ->
                val shouldRetry = attempt == 0L && !receivedEvent && cause.isTransientConnectionFailure()
                if (shouldRetry) delay(retryDelay)
                shouldRetry
            }
    }

    // Card-state probe: dedicated read-only endpoint, fast, no generation — so it skips
    // the long generation timeout used by streamDayStudy.
    suspend fun fetchStatus(request: DayStudyRequestDto): Result<DayStudyStatusDto> = suspendRunCatching {
        val body = requestJson.encodeToString(DayStudyRequestDto.serializer(), request)
        val response = functions.invoke(STATUS_FUNCTION_NAME) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        json.decodeFromString(DayStudyStatusDto.serializer(), response.bodyAsText())
    }

    private fun mapEvent(event: FunctionServerSentEvent): DayStudyStreamEvent? {
        val data = event.data ?: return null
        return when (event.event) {
            PROGRESS_EVENT -> DayStudyStreamEvent.Progress(
                phase = json.decodeFromString(DayStudyProgressDto.serializer(), data).phase,
            )

            COMPLETE_EVENT -> DayStudyStreamEvent.Complete(
                response = json.decodeFromString(DayStudyResponseDto.serializer(), data),
            )

            ERROR_EVENT -> throw IllegalStateException(data)

            else -> null
        }
    }

    private fun Throwable.isTransientConnectionFailure(): Boolean =
        this is HttpRequestException || (this is SSEClientException && response == null)

    companion object {
        private const val FUNCTION_NAME = "get-day-study"
        private const val STATUS_FUNCTION_NAME = "get-day-study-status"
        private const val PROGRESS_EVENT = "progress"
        private const val COMPLETE_EVENT = "complete"
        private const val ERROR_EVENT = "error"
        private const val REQUEST_TIMEOUT_MILLIS = 240_000L
        private const val SOCKET_TIMEOUT_MILLIS = 60_000L
    }
}
