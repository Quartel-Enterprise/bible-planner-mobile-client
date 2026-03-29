package com.quare.bibleplanner.feature.bibleversion.domain

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.supabase.domain.repository.SupabaseBucketRepository
import com.quare.bibleplanner.feature.bibleversion.data.dto.SyncChapterDto
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

internal class DownloadBibleUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val supabaseBucketRepository: SupabaseBucketRepository,
    private val getPentateuchIds: GetPentateuchIdsUseCase,
    private val getNewTestamentIds: GetNewTestamentIdsUseCase,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val successResult = Result.success(Unit)

    suspend operator fun invoke(versionId: String): Result<Unit> =
        runCatching {
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
            bibleVersionDao.updateStatus(
                id = versionId,
                status = DownloadStatus.DONE
            )
        }

    private suspend fun downloadChapters(
        versionId: String,
        progressMutex: Mutex,
        bookId: BookId,
    ): Result<Unit> {
        val supabaseBookDir = supabaseBookAbbreviationMapper.map(bookId)
        val chapters = chapterDao.getChaptersByBookId(bookId.name)
        chapters.chunked(DOWNLOAD_CHAPTERS_CHUNK_SIZE).forEach { chunk: List<ChapterEntity> ->
            chunk.forEach { chapter: ChapterEntity ->
                val exists = verseDao.countVersesByChapterAndVersion(
                    chapterId = chapter.id,
                    versionId = versionId
                ) > 0
                if (!exists) {
                    getChapterNumberDownloadResult(
                        versionId = versionId,
                        supabaseBookDir = supabaseBookDir,
                        chapterNumber = chapter.number
                    ).onSuccess { bytes ->
                        onChapterNumberDownloadSuccess(chapter, versionId, bytes, progressMutex)
                    }.onFailure {
                        return Result.failure(it)
                    }
                }
            }
        }
        return Result.success(Unit)
    }

    private suspend fun onChapterNumberDownloadSuccess(
        chapter: ChapterEntity,
        versionId: String,
        bytes: ByteArray,
        progressMutex: Mutex,
    ) {
        saveChapterToDatabase(
            chapterId = chapter.id,
            versionId = versionId,
            chapterDto = json.decodeFromString<SyncChapterDto>(bytes.decodeToString()),
        )
        progressMutex.withLock {
            // Re-calculate progress to be safe
            val currentCount = verseDao.countChaptersWithVersesByVersion(versionId)
            bibleVersionDao.updateDownloadProgress(
                id = versionId,
                progress = currentCount.toFloat() / TOTAL_CHAPTERS,
            )
        }
    }

    private suspend fun getChapterNumberDownloadResult(
        versionId: String,
        supabaseBookDir: String,
        chapterNumber: Int,
    ): Result<ByteArray> {
        val fileName = "bible/${versionId.uppercase()}/$supabaseBookDir/${chapterNumber}.json"
        return supabaseBucketRepository.getByteArrayResult(fileName)
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
