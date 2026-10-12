package com.quare.bibleplanner.core.chapterlistening.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioCacheDataSource
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioRemoteDataSource
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioRequestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioResponseDto
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDownloadException
import com.quare.bibleplanner.core.chapterlistening.data.exception.NotSignedInException
import com.quare.bibleplanner.core.chapterlistening.data.exception.isUnauthorized
import com.quare.bibleplanner.core.chapterlistening.data.mapper.ChapterAudioMapper
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.Url
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
    private val downloadLocks = mutableMapOf<String, ChapterLock>()

    // Why: a cached chapter is checked against the server once per launch, so a corrected revision replaces it.
    private val checkedChapterKeys = mutableSetOf<String>()
    private val checkingChapterKeys = mutableSetOf<String>()

    override suspend fun loadChapterAudio(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioResultModel {
        val request = toRequest(
            bibleVersionId = bibleVersionId,
            chapter = chapter,
        )
        val chapterKey = toChapterKey(request)
        findCached(chapterKey)?.let { cached -> return ChapterAudioResultModel.Ready(cached) }
        // Why: a chapter still being prefetched is awaited, not asked of the server a second time.
        return suspendRunCatching {
            withChapterLock(chapterKey) {
                findCached(chapterKey) ?: download(
                    request = request,
                    chapterKey = chapterKey,
                )
            }
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
        val shouldCheck = stateMutex.withLock {
            chapterKey !in checkedChapterKeys && checkingChapterKeys.add(chapterKey)
        }
        if (!shouldCheck) return
        // Why: a check that failed or was cancelled (offline, the chapter changed) is tried again the next time.
        try {
            suspendRunCatching {
                // Why: a chapter no longer cached has nothing to replace, so it costs no server call.
                if (cacheDataSource.findFileName(chapterKey) == null) return@suspendRunCatching
                val response = remoteDataSource.fetchChapterAudio(request)
                withChapterLock(chapterKey) {
                    val cachedFileName = cacheDataSource.findFileName(chapterKey)
                    if (cachedFileName != null && cachedFileName != toFileName(response.audioUrl)) {
                        save(
                            response = response,
                            chapterKey = chapterKey,
                        )
                    }
                }
            }.onSuccess {
                stateMutex.withLock { checkedChapterKeys += chapterKey }
            }.onFailure { throwable ->
                Logger.w(throwable) { "Could not check the cached audio of $chapterKey" }
            }
        } finally {
            withContext(NonCancellable) {
                stateMutex.withLock { checkingChapterKeys -= chapterKey }
            }
        }
    }

    override suspend fun evict(audioPath: String) {
        suspendRunCatching { cacheDataSource.remove(audioPath) }
            .onFailure { throwable -> Logger.e(throwable) { "Could not drop the cached audio $audioPath" } }
    }

    private fun toRequest(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioRequestDto = ChapterAudioRequestDto(
        book = bookIdWireNameMapper.map(chapter.bookId),
        chapter = chapter.chapterNumber,
        // Why: a version picked long ago may be stored in lower case, but the server lists them in upper case.
        version = bibleVersionId.uppercase(),
    )

    private fun toChapterKey(request: ChapterAudioRequestDto): String =
        listOf(request.version, request.book, request.chapter).joinToString(KEY_SEPARATOR)

    // Why: a lock no one holds or waits for is dropped, so listening through whole books doesn't keep one per chapter.
    private suspend fun <T> withChapterLock(
        chapterKey: String,
        block: suspend () -> T,
    ): T {
        val lock = stateMutex.withLock {
            downloadLocks.getOrPut(chapterKey, ::ChapterLock).also { it.holders++ }
        }
        try {
            return lock.mutex.withLock { block() }
        } finally {
            withContext(NonCancellable) {
                stateMutex.withLock {
                    lock.holders--
                    if (lock.holders == 0) downloadLocks -= chapterKey
                }
            }
        }
    }

    // Why: the cache is only a shortcut, so one that can't be read, or holds a broken manifest, means downloading.
    private suspend fun findCached(chapterKey: String): ChapterAudioModel? {
        val cached = suspendRunCatching { cacheDataSource.find(chapterKey) }
            .onFailure { throwable -> Logger.e(throwable) { "Could not read the cached audio of $chapterKey" } }
            .getOrNull() ?: return null
        return suspendRunCatching { chapterAudioMapper.map(cached) }
            .onFailure { throwable ->
                Logger.e(throwable) { "Unreadable cached manifest of $chapterKey" }
                suspendRunCatching { cacheDataSource.remove(cached.audioPath) }
                    .onFailure { removeFailure ->
                        Logger.e(removeFailure) { "Could not drop the audio of $chapterKey" }
                    }
            }.getOrNull()
    }

    private suspend fun download(
        request: ChapterAudioRequestDto,
        chapterKey: String,
    ): ChapterAudioModel {
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
    ): ChapterAudioModel = coroutineScope {
        /*
         * Why: the manifest downloads alongside the audio, and is parsed as soon as it arrives, so one that
         * can't be read cancels the audio download and is never cached.
         */
        val manifest = async {
            val manifestText = remoteDataSource.downloadText(response.manifestUrl)
            manifestText to chapterAudioMapper.parse(manifestText)
        }
        val cached = cacheDataSource.save(
            chapterKey = chapterKey,
            fileName = toFileName(response.audioUrl),
            readManifest = { manifest.await().first },
            writeAudio = { destination ->
                remoteDataSource.downloadFile(
                    url = response.audioUrl,
                    destination = destination,
                )
            },
        )
        chapterAudioMapper.map(
            audioPath = cached.audioPath,
            manifest = manifest.await().second,
        )
    }

    // Why: the URL path names the version, cast, text revision and chapter, so a new revision gets a new file.
    private fun toFileName(audioUrl: String): String {
        val path = Url(audioUrl).encodedPath.removePrefix(PATH_SEPARATOR)
        // Why: only the file's own extension goes, never a dot of a directory such as the text revision.
        val name = path.substringAfterLast(PATH_SEPARATOR).substringBeforeLast(EXTENSION_SEPARATOR)
        return (path.substringBeforeLast(PATH_SEPARATOR, missingDelimiterValue = "") + PATH_SEPARATOR + name)
            .removePrefix(PATH_SEPARATOR)
            .replace(PATH_SEPARATOR, FILE_NAME_SEPARATOR)
    }

    private fun toFailure(
        throwable: Throwable,
        chapterKey: String,
    ): ChapterAudioFailureModel {
        val failure = when (throwable) {
            is NotSignedInException -> ChapterAudioFailureModel.NOT_SIGNED_IN

            is RestException -> toFailure(throwable)

            is ChapterAudioDownloadException -> if (throwable.isUnauthorized()) {
                ChapterAudioFailureModel.NOT_SIGNED_IN
            } else {
                ChapterAudioFailureModel.UNAVAILABLE
            }

            /*
             * Why: only the function call has a request timeout, and one that runs out reached a server
             * that may still be generating the chapter; a connection that never opens or stalls is offline.
             */
            is HttpRequestTimeoutException -> ChapterAudioFailureModel.UNAVAILABLE

            is HttpRequestException -> ChapterAudioFailureModel.NO_CONNECTION

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

    private fun toFailure(exception: RestException): ChapterAudioFailureModel {
        if (exception.isUnauthorized()) return ChapterAudioFailureModel.NOT_SIGNED_IN
        return when (readErrorCode(exception.error)) {
            VERSION_NOT_ENABLED_CODE -> ChapterAudioFailureModel.VERSION_NOT_ENABLED
            BUDGET_REACHED_CODE -> ChapterAudioFailureModel.BUDGET_REACHED
            else -> ChapterAudioFailureModel.UNAVAILABLE
        }
    }

    private fun readErrorCode(body: String): String? = suspendRunCatching {
        json
            .parseToJsonElement(body)
            .jsonObject[CODE_FIELD]
            ?.jsonPrimitive
            ?.contentOrNull
    }.getOrNull()

    private class ChapterLock {
        val mutex = Mutex()
        var holders = 0
    }

    private companion object {
        const val KEY_SEPARATOR = "/"
        const val PATH_SEPARATOR = "/"
        const val EXTENSION_SEPARATOR = "."
        const val FILE_NAME_SEPARATOR = "_"
        const val CODE_FIELD = "code"
        const val BUDGET_REACHED_CODE = "MONTHLY_BUDGET_REACHED"
        const val VERSION_NOT_ENABLED_CODE = "VERSION_NOT_ENABLED"
    }
}
