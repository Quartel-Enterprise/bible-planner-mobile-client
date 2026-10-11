package com.quare.bibleplanner.core.chapterlistening.data.datasource

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioRequestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioResponseDto
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDownloadException
import com.quare.bibleplanner.core.chapterlistening.data.exception.NotSignedInException
import com.quare.bibleplanner.core.chapterlistening.data.exception.isUnauthorized
import com.quare.bibleplanner.core.chapterlistening.data.exception.storeChapterAudio
import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState
import com.quare.bibleplanner.core.user.domain.usecase.CheckRemoteSessionState
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.IOException
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json

internal class ChapterAudioRemoteDataSourceImpl(
    private val functions: Functions,
    private val httpClient: HttpClient,
    private val auth: Auth,
    private val json: Json,
    private val checkRemoteSessionState: CheckRemoteSessionState,
) : ChapterAudioRemoteDataSource {
    /*
     * Why: a chapter nobody has heard yet is generated during this call (~20–35 s) without sending a
     * byte, and a request for one already being generated waits up to 45 s on the server, both past
     * the client's 30 s request and OkHttp's 10 s read defaults. Only the request timeout applies,
     * since Supabase reports any other timeout as the missing connection a failed request means.
     */
    override suspend fun fetchChapterAudio(request: ChapterAudioRequestDto): ChapterAudioResponseDto =
        withFreshSession {
            val response = functions.invoke(FUNCTION_NAME) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ChapterAudioRequestDto.serializer(), request))
                timeout {
                    requestTimeoutMillis = FUNCTION_TIMEOUT_MILLIS
                    socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }
            }
            json.decodeFromString(ChapterAudioResponseDto.serializer(), response.bodyAsText())
        }

    override suspend fun downloadText(url: String): String = withFreshSession {
        get(url) { response -> response.bodyAsText() }
    }

    override suspend fun downloadFile(
        url: String,
        destination: Path,
    ) {
        withFreshSession {
            get(url) { response ->
                val channel = response.bodyAsChannel()
                val file = storeChapterAudio { SystemFileSystem.sink(destination).buffered() }
                // Why: the last buffered bytes are flushed on close, so a full disk can surface only there.
                try {
                    while (!channel.exhausted()) {
                        val chunk = channel.readRemaining(CHUNK_BYTES)
                        storeChapterAudio { file.transferFrom(chunk) }
                    }
                } catch (exception: Throwable) {
                    file.closeQuietly()
                    throw exception
                }
                storeChapterAudio { file.close() }
            }
        }
    }

    /*
     * Why: the access token isn't refreshed while the app is in the background, so a chapter that
     * starts after an hour of listening with the screen off is refused until the token is renewed.
     */
    private suspend fun <T> withFreshSession(call: suspend () -> T): T {
        val usedToken = auth.currentAccessTokenOrNull()
        val result = suspendRunCatching { call() }
        val failure = result.exceptionOrNull()
        if (failure == null || !failure.isUnauthorized() || auth.currentSessionOrNull() == null) {
            return result.getOrThrow()
        }
        if (auth.currentAccessTokenOrNull() == usedToken) renewSession()
        return call()
    }

    // Why: like Supabase's own auto refresh, a refused refresh token ends the session, so the person can sign in again.
    private suspend fun renewSession() {
        when (checkRemoteSessionState()) {
            RemoteSessionState.ACTIVE -> Unit

            RemoteSessionState.REVOKED -> {
                auth.clearSession()
                throw NotSignedInException()
            }

            RemoteSessionState.UNKNOWN -> throw IOException("Could not renew the session")
        }
    }

    // Why: the Worker serves the files only to a signed-in user, with the same token the function takes.
    private suspend fun <T> get(
        url: String,
        read: suspend (HttpResponse) -> T,
    ): T {
        val token = auth.currentAccessTokenOrNull() ?: throw NotSignedInException()
        return httpClient.prepareGet(url) { bearerAuth(token) }.execute { response ->
            if (!response.status.isSuccess()) throw ChapterAudioDownloadException(response.status.value)
            read(response)
        }
    }

    private fun Sink.closeQuietly() {
        runCatching { close() }.onFailure { throwable -> Logger.w(throwable) { "Could not close the audio file" } }
    }

    private companion object {
        const val FUNCTION_NAME = "get-chapter-audio"
        const val FUNCTION_TIMEOUT_MILLIS = 90_000L
        const val CHUNK_BYTES = 64L * 1024
    }
}
