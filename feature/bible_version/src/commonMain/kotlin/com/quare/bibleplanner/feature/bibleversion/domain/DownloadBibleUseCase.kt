package com.quare.bibleplanner.feature.bibleversion.domain

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.feature.bibleversion.data.dto.SyncChapterDto
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

internal class DownloadBibleUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val bucketApi: BucketApi,
    private val getPentateuchIds: GetPentateuchIdsUseCase,
    private val getNewTestamentIds: GetNewTestamentIdsUseCase,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val successResult = Result.success(Unit)

    suspend operator fun invoke(versionId: String): Result<Unit> {
        try {
            val version = bibleVersionDao.getVersionById(versionId)
                ?: return Result.failure(IllegalStateException("Version not found"))

            if (version.status == DownloadStatus.DONE) return successResult

            val pentateuch = getPentateuchIds()
            val newTestament = getNewTestamentIds()
            val rest = BookId.entries.filter { it !in pentateuch && it !in newTestament }
            val prioritizedBookIds = pentateuch + newTestament + rest

            val progressMutex = Mutex()

            prioritizedBookIds.forEach { bookId ->
                downloadChapters(versionId, progressMutex, bookId)
            }

            // Mark as DONE after all chapters are downloaded
            bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE)
            return successResult
        } catch (e: Exception) {
            Logger.e { "Global sync error: ${e.message}" }
            return Result.failure(e)
        }
    }

    private suspend fun downloadChapters(
        versionId: String,
        progressMutex: Mutex,
        bookId: BookId,
    ) {
        val supabaseBookDir = supabaseBookAbbreviationMapper.map(bookId)
        val chapters = chapterDao.getChaptersByBookId(bookId.name)
        chapters.chunked(DOWNLOAD_CHAPTERS_CHUNK_SIZE).forEach { chunk ->
            chunk
                .map { chapter ->
                    syncScope.launch {
                        try {
                            val exists = verseDao.countVersesByChapterAndVersion(chapter.id, versionId) > 0

                            if (!exists) {
                                val fileName = "bible/${versionId.uppercase()}/$supabaseBookDir/${chapter.number}.json"
                                val bytes = bucketApi.downloadPublic(fileName)
                                saveChapterToDatabase(
                                    chapterId = chapter.id,
                                    versionId = versionId,
                                    chapterDto = json.decodeFromString<SyncChapterDto>(bytes.decodeToString()),
                                )
                            }

                            progressMutex.withLock {
                                // Re-calculate progress to be safe
                                val currentCount = verseDao.countChaptersWithVersesByVersion(versionId)
                                bibleVersionDao.updateDownloadProgress(
                                    id = versionId,
                                    progress = currentCount.toFloat() / TOTAL_CHAPTERS,
                                )
                            }
                        } catch (e: Exception) {
                            Logger.e { "Error syncing $bookId:${chapter.number}: ${e.message}" }
                        }
                    }
                }.joinAll()
        }
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
                )
            }
        }
    }

    companion object {
        private const val TOTAL_CHAPTERS = 1189
        private const val DOWNLOAD_CHAPTERS_CHUNK_SIZE = 10
    }
}
