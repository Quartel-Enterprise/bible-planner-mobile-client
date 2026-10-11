package com.quare.bibleplanner.core.chapterlistening.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioCacheDataSource
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioRemoteDataSource
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioRequestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioResponseDto
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDownloadException
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioStorageException
import com.quare.bibleplanner.core.chapterlistening.data.exception.NotSignedInException
import com.quare.bibleplanner.core.chapterlistening.data.mapper.ChapterAudioMapper
import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class ChapterAudioRepositoryImpl(
    private val remoteDataSource: ChapterAudioRemoteDataSource,
    private val cacheDataSource: ChapterAudioCacheDataSource,
    private val bookIdWireNameMapper: BookIdWireNameMapper,
    private val chapterAudioMapper: ChapterAudioMapper,
    private val json: Json,
) : ChapterAudioRepository {
    private val stateMutex = Mutex()

    // Why: playing a chapter while it is being prefetched waits for that download, but never for another chapter's.
    private val downloadLocks = mutableMapOf<String, Mutex>()

    // Why: a cached chapter is checked against the server once per launch, so a corrected revision replaces it.
    private val checkedChapterKeys = mutableSetOf<String>()

    override suspend fun getChapterAudio(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioResultModel {
        val request = toRequest(
            bibleVersionId = bibleVersionId,
            chapter = chapter,
        )
        val chapterKey = toChapterKey(request)
        findCached(chapterKey)?.let { cached -> return cached }
        // Why: a chapter still being prefetched is awaited, not asked of the server a second time.
        return suspendRunCatching {
            val cached = lockFor(chapterKey).withLock {
                cacheDataSource.find(chapterKey) ?: download(
                    request = request,
                    chapterKey = chapterKey,
                )
            }
            chapterAudioMapper.map(cached)
        }.fold(
            onSuccess = ChapterAudioResultModel::Ready,
            onFailure = { throwable -> ChapterAudioResultModel.Failed(toFailure(throwable, chapterKey)) },
        )
    }

    override suspend fun refresh(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ) {
        val request = toRequest(
            bibleVersionId = bibleVersionId,
            chapter = chapter,
        )
        val chapterKey = toChapterKey(request)
        val isFirstCheck = stateMutex.withLock { checkedChapterKeys.add(chapterKey) }
        if (!isFirstCheck) return
        suspendRunCatching {
            val response = remoteDataSource.fetchChapterAudio(request)
            lockFor(chapterKey).withLock {
                val cached = cacheDataSource.find(chapterKey)
                if (cached != null && cached.fileName != toFileName(response.audioUrl)) {
                    save(
                        response = response,
                        chapterKey = chapterKey,
                    )
                }
            }
        }.onFailure { throwable ->
            // Why: a check that failed (offline, say) is tried again the next time, not skipped until the next launch.
            stateMutex.withLock { checkedChapterKeys -= chapterKey }
            Logger.w(throwable) { "Could not check the cached audio of $chapterKey" }
        }
    }

    override suspend fun evict(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ) {
        val chapterKey = toChapterKey(
            toRequest(
                bibleVersionId = bibleVersionId,
                chapter = chapter,
            ),
        )
        suspendRunCatching { cacheDataSource.remove(chapterKey) }
            .onFailure { throwable -> Logger.e(throwable) { "Could not drop the cached audio of $chapterKey" } }
    }

    private fun toRequest(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioRequestDto = ChapterAudioRequestDto(
        book = bookIdWireNameMapper.map(chapter.bookId),
        chapter = chapter.chapterNumber,
        version = bibleVersionId,
    )

    private fun toChapterKey(request: ChapterAudioRequestDto): String =
        listOf(request.version, request.book, request.chapter).joinToString(KEY_SEPARATOR)

    private suspend fun lockFor(chapterKey: String): Mutex = stateMutex.withLock {
        downloadLocks.getOrPut(chapterKey, ::Mutex)
    }

    private suspend fun findCached(chapterKey: String): ChapterAudioResultModel? {
        val cached = suspendRunCatching { cacheDataSource.find(chapterKey) }
            .onFailure { throwable -> Logger.e(throwable) { "Could not read the cached audio of $chapterKey" } }
            .getOrNull() ?: return null
        return suspendRunCatching { ChapterAudioResultModel.Ready(chapterAudioMapper.map(cached)) }
            .onFailure { throwable ->
                Logger.e(throwable) { "Unreadable cached manifest of $chapterKey" }
                suspendRunCatching { cacheDataSource.remove(chapterKey) }
                    .onFailure { removeFailure ->
                        Logger.e(removeFailure) { "Could not drop the audio of $chapterKey" }
                    }
            }.getOrNull()
    }

    private suspend fun download(
        request: ChapterAudioRequestDto,
        chapterKey: String,
    ): CachedChapterAudio {
        val response = remoteDataSource.fetchChapterAudio(request)
        stateMutex.withLock { checkedChapterKeys += chapterKey }
        return save(
            response = response,
            chapterKey = chapterKey,
        )
    }

    private suspend fun save(
        response: ChapterAudioResponseDto,
        chapterKey: String,
    ): CachedChapterAudio {
        val manifest = remoteDataSource.downloadText(response.manifestUrl)
        return cacheDataSource.save(
            chapterKey = chapterKey,
            fileName = toFileName(response.audioUrl),
            manifest = manifest,
            writeAudio = { destination ->
                remoteDataSource.downloadFile(
                    url = response.audioUrl,
                    destination = destination,
                )
            },
        )
    }

    // Why: the URL path names the version, cast, text revision and chapter, so a new revision gets a new file.
    private fun toFileName(audioUrl: String): String = audioUrl
        .substringAfter(URL_SCHEME_SEPARATOR)
        .substringAfter(PATH_SEPARATOR)
        .substringBeforeLast(EXTENSION_SEPARATOR)
        .replace(PATH_SEPARATOR, FILE_NAME_SEPARATOR)

    private fun toFailure(
        throwable: Throwable,
        chapterKey: String,
    ): ChapterAudioFailureModel {
        val failure = when (throwable) {
            is NotSignedInException -> ChapterAudioFailureModel.NOT_SIGNED_IN

            is RestException -> toFailure(throwable)

            is ChapterAudioDownloadException -> if (throwable.statusCode == UNAUTHORIZED_STATUS) {
                ChapterAudioFailureModel.NOT_SIGNED_IN
            } else {
                ChapterAudioFailureModel.UNAVAILABLE
            }

            // Why: a request that times out reached the network, and the server may still be generating the chapter.
            is HttpRequestTimeoutException,
            is ConnectTimeoutException,
            is SocketTimeoutException,
            -> ChapterAudioFailureModel.UNAVAILABLE

            is HttpRequestException -> ChapterAudioFailureModel.NO_CONNECTION

            is ChapterAudioStorageException -> ChapterAudioFailureModel.UNAVAILABLE

            is IOException -> ChapterAudioFailureModel.NO_CONNECTION

            else -> ChapterAudioFailureModel.UNAVAILABLE
        }
        if (failure == ChapterAudioFailureModel.UNAVAILABLE && throwable !is RestException) {
            Logger.e(throwable) { "Could not get the chapter audio of $chapterKey" }
        } else {
            Logger.w(throwable) { "No chapter audio for $chapterKey: $failure" }
        }
        return failure
    }

    private fun toFailure(exception: RestException): ChapterAudioFailureModel = when (exception.statusCode) {
        UNAUTHORIZED_STATUS -> ChapterAudioFailureModel.NOT_SIGNED_IN

        FORBIDDEN_STATUS -> ChapterAudioFailureModel.VERSION_NOT_ENABLED

        else -> if (readErrorCode(exception.error) == BUDGET_REACHED_CODE) {
            ChapterAudioFailureModel.BUDGET_REACHED
        } else {
            ChapterAudioFailureModel.UNAVAILABLE
        }
    }

    private fun readErrorCode(body: String): String? = suspendRunCatching {
        json
            .parseToJsonElement(body)
            .jsonObject[CODE_FIELD]
            ?.jsonPrimitive
            ?.contentOrNull
    }.getOrNull()

    private companion object {
        const val KEY_SEPARATOR = "/"
        const val URL_SCHEME_SEPARATOR = "://"
        const val PATH_SEPARATOR = "/"
        const val EXTENSION_SEPARATOR = "."
        const val FILE_NAME_SEPARATOR = "_"
        const val CODE_FIELD = "code"
        const val BUDGET_REACHED_CODE = "MONTHLY_BUDGET_REACHED"
        const val UNAUTHORIZED_STATUS = 401
        const val FORBIDDEN_STATUS = 403
    }
}
