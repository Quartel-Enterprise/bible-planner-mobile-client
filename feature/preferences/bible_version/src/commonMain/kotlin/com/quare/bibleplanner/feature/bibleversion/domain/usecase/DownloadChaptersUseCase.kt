package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.data.dto.SyncChapterDto
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DownloadChaptersUseCase(
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val bucketApi: BucketApi,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val downloadSemaphore = Semaphore(permits = MAX_CONCURRENT_DOWNLOADS)
    private val retryDelay: Duration = 2.seconds

    suspend operator fun invoke(
        versionId: String,
        bookId: BookId,
    ): Result<Unit> = suspendRunCatching {
        val supabaseBookDir = supabaseBookAbbreviationMapper.map(bookId)
        val chapters = chapterDao.getChaptersByBookId(bookId.name)
        val downloadedChapterIds = verseDao
            .getDownloadedChapterIds(
                versionId = versionId,
                chapterIds = chapters.map { it.id },
            ).toSet()
        var failedChapters = 0
        chapters
            .filterNot { it.id in downloadedChapterIds }
            .chunked(DOWNLOAD_CHAPTERS_CHUNK_SIZE)
            .forEach { chunk ->
                supervisorScope {
                    val results = chunk
                        .map { chapter ->
                            async {
                                suspendRunCatching {
                                    val fileName =
                                        "bible/${versionId.uppercase()}/$supabaseBookDir/${chapter.number}.json"
                                    val bytes = downloadChapterBytes(fileName)
                                    chapter.id to json.decodeFromString<SyncChapterDto>(bytes.decodeToString())
                                }.onFailure { Logger.e(it) { "Error syncing $bookId:${chapter.number}" } }
                            }
                        }.awaitAll()
                    failedChapters += results.count { it.isFailure }
                    saveChaptersToDatabase(
                        versionId = versionId,
                        chapters = results.mapNotNull { it.getOrNull() }.toMap(),
                    )
                }
            }
        check(failedChapters == 0) { "$failedChapters chapters of $bookId failed to download" }
    }.onFailure { Logger.e(it) { "Error downloading chapters for $bookId" } }

    private suspend fun downloadChapterBytes(fileName: String): ByteArray {
        repeat(MAX_DOWNLOAD_ATTEMPTS - 1) {
            suspendRunCatching {
                downloadSemaphore.withPermit { bucketApi.downloadPublic(fileName) }
            }.onSuccess { bytes -> return bytes }
                .onFailure { Logger.e(it) { "Retrying $fileName after a failed attempt" } }
            delay(retryDelay)
        }
        return downloadSemaphore.withPermit { bucketApi.downloadPublic(fileName) }
    }

    /**
     * A whole chunk lands in one write. Every write wakes every screen observing the Bible tables, so
     * saving chapter by chapter is what makes the app stutter while a version downloads.
     */
    private suspend fun saveChaptersToDatabase(
        versionId: String,
        chapters: Map<Long, SyncChapterDto>,
    ) {
        if (chapters.isEmpty()) return
        val versesByChapter = verseDao
            .getVersesByChapterIds(chapters.keys.toList())
            .groupBy { it.chapterId }
        val verseTextEntities = chapters.flatMap { (chapterId, chapterDto) ->
            val existingVerses = versesByChapter[chapterId].orEmpty().associateBy { it.number }
            chapterDto.verses.mapNotNull { verseDto ->
                existingVerses[verseDto.number]?.let { verseEntity ->
                    VerseTextEntity(
                        verseId = verseEntity.id,
                        bibleVersionId = versionId,
                        text = verseDto.text,
                        heading = verseDto.heading,
                    )
                }
            }
        }
        verseDao.upsertVerseTexts(verseTextEntities)
    }

    companion object {
        private const val DOWNLOAD_CHAPTERS_CHUNK_SIZE = 10

        /**
         * The real ceiling on the download, kept below the HTTP client's own per-host limit so the
         * app's other Supabase calls are never stuck behind a download burst. Supabase serves these
         * chapters from the CDN edge and answered far more than this without throttling.
         */
        private const val MAX_CONCURRENT_DOWNLOADS = 24
        private const val MAX_DOWNLOAD_ATTEMPTS = 3
    }
}
