package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.data.dto.AskChatRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDeltaDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDoneDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatStatusDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatTitleDto
import com.quare.bibleplanner.feature.chat.data.exception.ChatStreamStalledException
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.functions.FunctionServerSentEvent
import io.github.jan.supabase.functions.Functions
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.timeout
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ChatStreamRemoteDataSource(
    private val functions: Functions,
    private val json: Json,
) {
    private val requestJson = Json { explicitNulls = false }
    private val retryDelay: Duration = 1.seconds
    private val streamIdleTimeout: Duration = 60.seconds

    @OptIn(FlowPreview::class)
    fun streamAnswer(request: AskChatRequestDto): Flow<ChatStreamEvent> {
        val body = requestJson.encodeToString(AskChatRequestDto.serializer(), request)
        var receivedEvent = false
        return functions
            .invokeSSE(FUNCTION_NAME) {
                contentType(ContentType.Application.Json)
                setBody(body)
                timeout {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
                }
            }.timeout(streamIdleTimeout)
            .catch { throwable ->
                if (throwable is TimeoutCancellationException) {
                    throw ChatStreamStalledException()
                } else {
                    throw throwable
                }
            }.onEach { receivedEvent = true }
            .mapNotNull(::mapEvent)
            .retryWhen { cause, attempt ->
                val shouldRetry = attempt == 0L && !receivedEvent && cause.isTransientConnectionFailure()
                if (shouldRetry) delay(retryDelay)
                shouldRetry
            }
    }

    suspend fun fetchStatus(): Result<ChatStatusDto> = suspendRunCatching {
        val response = functions.invoke(STATUS_FUNCTION_NAME) {
            contentType(ContentType.Application.Json)
            setBody(EMPTY_BODY)
        }
        json.decodeFromString(ChatStatusDto.serializer(), response.bodyAsText())
    }

    private fun mapEvent(event: FunctionServerSentEvent): ChatStreamEvent? {
        val data = event.data ?: return null
        return when (event.event) {
            ACCEPTED_EVENT -> ChatStreamEvent.Accepted(json.decodeFromString(ChatAcceptedDto.serializer(), data))

            DELTA_EVENT -> ChatStreamEvent.Delta(json.decodeFromString(ChatDeltaDto.serializer(), data).text)

            RESTART_EVENT -> ChatStreamEvent.Restart

            DONE_EVENT -> ChatStreamEvent.Done(json.decodeFromString(ChatDoneDto.serializer(), data))

            TITLE_EVENT -> json.decodeFromString(ChatTitleDto.serializer(), data).let { title ->
                ChatStreamEvent.Title(
                    conversationId = title.conversationId,
                    title = title.title,
                )
            }

            ERROR_EVENT -> throw IllegalStateException(data)

            else -> null
        }
    }

    private fun Throwable.isTransientConnectionFailure(): Boolean =
        this is HttpRequestException || (this is SSEClientException && response == null)

    companion object {
        private const val FUNCTION_NAME = "ask-chat"
        private const val STATUS_FUNCTION_NAME = "get-chat-status"
        private const val ACCEPTED_EVENT = "accepted"
        private const val DELTA_EVENT = "delta"
        private const val RESTART_EVENT = "restart"
        private const val DONE_EVENT = "done"
        private const val TITLE_EVENT = "title"
        private const val ERROR_EVENT = "error"
        private const val EMPTY_BODY = "{}"
        private const val REQUEST_TIMEOUT_MILLIS = 180_000L
        private const val SOCKET_TIMEOUT_MILLIS = 60_000L
    }
}
