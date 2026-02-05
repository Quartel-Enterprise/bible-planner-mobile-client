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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
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
    private val supabaseClient: SupabaseClient,
    private val bibleVersionDao: BibleVersionDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val bucketApi: BucketApi,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend operator fun invoke(versionId: String) {
        try {
            val version = bibleVersionDao.getVersionById(versionId)
                ?: return

            if (version.status == DownloadStatus.DONE) return

            val pentateuch = listOf(BookId.GEN, BookId.EXO, BookId.LEV, BookId.NUM, BookId.DEU)
            val newTestament = BookId.entries.filter { it.ordinal >= BookId.MAT.ordinal }
            val rest = BookId.entries.filter { it !in pentateuch && it !in newTestament }
            val prioritizedBooks = pentateuch + newTestament + rest

            val progressMutex = Mutex()
            val lowerVersionId = versionId.uppercase()

            for (bookId in prioritizedBooks) {
                downloadChapters(versionId, lowerVersionId, progressMutex, bookId)
            }

            // Mark as DONE after all chapters are downloaded
            bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE)
        } catch (e: Exception) {
            Logger.e { "Global sync error: ${e.message}" }
        }
    }

    private suspend fun downloadChapters(
        versionId: String,
        lowerVersionId: String,
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
                                val fileName = "bible/$lowerVersionId/$supabaseBookDir/${chapter.number}.json"
                                val bytes = bucketApi.downloadPublic(fileName)
                                val chapterDto = json.decodeFromString<SyncChapterDto>(bytes.decodeToString())

                                saveChapterToDatabase(
                                    chapterId = chapter.id,
                                    versionId = versionId,
                                    chapterDto = chapterDto,
                                )
                            }

                            progressMutex.withLock {
                                // Re-calculate progress to be safe
                                val currentCount = verseDao.countChaptersWithVersesByVersion(versionId)
                                val progress = currentCount.toFloat() / TOTAL_CHAPTERS
                                bibleVersionDao.updateDownloadProgress(versionId, progress)
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
