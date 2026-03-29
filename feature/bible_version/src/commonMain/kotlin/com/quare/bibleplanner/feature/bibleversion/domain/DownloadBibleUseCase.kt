package com.quare.bibleplanner.feature.bibleversion.domain

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

            prioritizedBookIds.forEach { bookId ->
                downloadChapters(versionId, bookId)
            }

            // Mark as DONE after all chapters are downloaded
            bibleVersionDao.updateStatus(
                id = versionId,
                status = DownloadStatus.DONE
            )
        }

    private suspend fun downloadChapters(
        versionId: String,
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
                        saveChapterToDatabase(
                            chapterId = chapter.id,
                            versionId = versionId,
                            chapterDto = json.decodeFromString<SyncChapterDto>(bytes.decodeToString()),
                        )
                    }.onFailure {
                        return Result.failure(it)
                    }
                }
            }
        }
        return Result.success(Unit)
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
        private const val DOWNLOAD_CHAPTERS_CHUNK_SIZE = 10
    }
}
