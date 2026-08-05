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
        var failedChapters = 0
        chapters.chunked(DOWNLOAD_CHAPTERS_CHUNK_SIZE).forEach { chunk ->
            supervisorScope {
                val results = chunk
                    .map { chapter ->
                        async {
                            suspendRunCatching {
                                val exists = verseDao.countVersesByChapterAndVersion(chapter.id, versionId) > 0
                                if (!exists) {
                                    val fileName =
                                        "bible/${versionId.uppercase()}/$supabaseBookDir/${chapter.number}.json"
                                    val bytes = downloadChapterBytes(fileName)
                                    saveChapterToDatabase(
                                        chapterId = chapter.id,
                                        versionId = versionId,
                                        chapterDto = json.decodeFromString<SyncChapterDto>(bytes.decodeToString()),
                                    )
                                }
                            }.onFailure { Logger.e(it) { "Error syncing $bookId:${chapter.number}" } }
                        }
                    }.awaitAll()
                failedChapters += results.count { it.isFailure }
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

    private suspend fun saveChapterToDatabase(
        chapterId: Long,
        versionId: String,
        chapterDto: SyncChapterDto,
    ) {
        val verseTextEntities = getVerseTextEntities(chapterId, chapterDto, versionId)
        verseDao.upsertVerseTexts(verseTextEntities)
    }

    private suspend fun getVerseTextEntities(
        chapterId: Long,
        chapterDto: SyncChapterDto,
        versionId: String,
    ): List<VerseTextEntity> {
        val existingVerses = verseDao.getVersesByChapterId(chapterId).associateBy { it.number }
        return chapterDto.verses.mapNotNull { verseDto ->
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

    companion object {
        private const val DOWNLOAD_CHAPTERS_CHUNK_SIZE = 10
        private const val MAX_CONCURRENT_DOWNLOADS = 10
        private const val MAX_DOWNLOAD_ATTEMPTS = 3
    }
}
