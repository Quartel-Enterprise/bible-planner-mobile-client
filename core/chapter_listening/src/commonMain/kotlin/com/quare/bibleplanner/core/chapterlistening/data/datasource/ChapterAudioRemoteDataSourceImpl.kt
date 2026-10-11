package com.quare.bibleplanner.core.chapterlistening.data.datasource

import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioRequestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioResponseDto
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDownloadException
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioStorageException
import com.quare.bibleplanner.core.chapterlistening.data.exception.NotSignedInException
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.ktor.client.HttpClient
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
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json

internal class ChapterAudioRemoteDataSourceImpl(
    private val functions: Functions,
    private val httpClient: HttpClient,
    private val auth: Auth,
    private val json: Json,
) : ChapterAudioRemoteDataSource {
    /*
     * Why: a chapter nobody has heard yet is generated during this call (~20–35 s), and a request for
     * one already being generated waits up to 45 s on the server, both past the client's 30 s default.
     */
    override suspend fun fetchChapterAudio(request: ChapterAudioRequestDto): ChapterAudioResponseDto {
        val response = functions.invoke(FUNCTION_NAME) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(ChapterAudioRequestDto.serializer(), request))
            timeout { requestTimeoutMillis = FUNCTION_TIMEOUT_MILLIS }
        }
        return json.decodeFromString(ChapterAudioResponseDto.serializer(), response.bodyAsText())
    }

    override suspend fun downloadText(url: String): String = get(url) { response -> response.bodyAsText() }

    override suspend fun downloadFile(
        url: String,
        destination: Path,
    ) {
        get(url) { response ->
            val channel = response.bodyAsChannel()
            val sink = store { SystemFileSystem.sink(destination).buffered() }
            sink.use { file ->
                while (!channel.exhausted()) {
                    write(
                        file = file,
                        chunk = channel.readRemaining(CHUNK_BYTES),
                    )
                }
            }
        }
    }

    private fun write(
        file: Sink,
        chunk: Source,
    ) {
        store { file.transferFrom(chunk) }
    }

    // Why: a full disk must not be reported as the missing connection a failed read means.
    private inline fun <T> store(block: () -> T): T = try {
        block()
    } catch (exception: IOException) {
        throw ChapterAudioStorageException(exception)
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

    private companion object {
        const val FUNCTION_NAME = "get-chapter-audio"
        const val FUNCTION_TIMEOUT_MILLIS = 90_000L
        const val CHUNK_BYTES = 64L * 1024
    }
}
