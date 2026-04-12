package com.quare.bibleplanner.worker

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.supabase.generated.SupabaseBuildKonfig
import com.quare.bibleplanner.feature.bibleversion.data.dto.SyncChapterDto
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class IosBackgroundDownloadBridge(
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val bibleVersionDao: BibleVersionDao,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
    private val json: Json,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val supabaseStorageBaseUrl: String =
        "${SupabaseBuildKonfig.SUPABASE_URL}/storage/v1/object/public/content"

    internal suspend fun getPendingDownloads(versionId: String): List<ChapterDownloadTask> =
        BookId.entries.flatMap { bookId ->
            val bookAbb = supabaseBookAbbreviationMapper.map(bookId)
            chapterDao.getChaptersByBookId(bookId.name)
                .filter { chapter -> verseDao.countVersesByChapterAndVersion(chapter.id, versionId) == 0 }
                .map { chapter ->
                    ChapterDownloadTask(
                        url = "$supabaseStorageBaseUrl/bible/${versionId.uppercase()}/$bookAbb/${chapter.number}.json",
                        versionId = versionId,
                        chapterId = chapter.id,
                    )
                }
        }

    fun processDownloadedChapter(
        chapterId: Long,
        versionId: String,
        jsonString: String,
        onComplete: () -> Unit,
    ) {
        scope.launch {
            try {
                val chapterDto = json.decodeFromString<SyncChapterDto>(jsonString)
                val existingVerses = verseDao.getVersesByChapterId(chapterId).associateBy { it.number }
                val verseTextEntities = chapterDto.verses.mapNotNull { verseDto ->
                    existingVerses[verseDto.number]?.let { verseEntity ->
                        VerseTextEntity(
                            verseId = verseEntity.id,
                            bibleVersionId = versionId,
                            text = verseDto.text,
                        )
                    }
                }
                verseDao.upsertVerseTexts(verseTextEntities)
            } catch (e: Exception) {
                Logger.e(e) { "Error processing downloaded chapter $chapterId for $versionId" }
            } finally {
                onComplete()
            }
        }
    }

    fun finalizeVersionIfComplete(versionId: String, onComplete: () -> Unit) {
        scope.launch {
            try {
                val entity = bibleVersionDao.getVersionById(versionId) ?: return@launch
                if (entity.status == DownloadStatus.DONE) return@launch
                val downloaded = verseDao.countChaptersWithVersesByVersion(versionId)
                if (downloaded >= entity.totalChapters) {
                    val name = resolveVersionName(versionId)
                    bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE)
                    notifier.showComplete(versionId, name)
                }
            } catch (e: Exception) {
                Logger.e(e) { "Error finalizing version $versionId" }
            } finally {
                onComplete()
            }
        }
    }

    private suspend fun resolveVersionName(versionId: String): String =
        bibleRepository.getBiblesFlow()
            .first()
            .find { it.version.id == versionId }
            ?.version
            ?.name ?: versionId
}
