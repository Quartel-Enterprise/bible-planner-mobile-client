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
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetRemoteContentVersionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

class IosBackgroundDownloadBridge(
    private val supabaseBookAbbreviationMapper: SupabaseBookAbbreviationMapper,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val bibleVersionDao: BibleVersionDao,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
    private val getRemoteContentVersion: GetRemoteContentVersionUseCase,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * How many chapters of each version are on disk, kept in step with the writes instead of being
     * counted again per chapter: counting walks every downloaded verse of every version, and a
     * download would pay for that walk once per chapter. Seeded when the download is planned, and
     * again on the first chapter of a session iOS resumed on its own after the app was relaunched.
     */
    private val downloadedChapters = mutableMapOf<String, Int>()
    private val downloadedChaptersMutex = Mutex()

    val supabaseStorageBaseUrl: String =
        "${SupabaseBuildKonfig.SUPABASE_URL}/storage/v1/object/public/content"

    internal suspend fun getPendingDownloads(versionId: String): List<ChapterDownloadTask> {
        val tasks = findPendingDownloads(versionId)
        val totalChapters = bibleVersionDao.getVersionById(versionId)?.totalChapters ?: 0
        downloadedChaptersMutex.withLock {
            downloadedChapters[versionId] = totalChapters - tasks.size
        }
        return tasks
    }

    private suspend fun findPendingDownloads(versionId: String): List<ChapterDownloadTask> =
        BookId.entries.flatMap { bookId ->
            val bookAbb = supabaseBookAbbreviationMapper.map(bookId)
            val chapters = chapterDao.getChaptersByBookId(bookId.name)
            val downloadedChapterIds = verseDao
                .getDownloadedChapterIds(
                    versionId = versionId,
                    chapterIds = chapters.map { it.id },
                ).toSet()
            chapters
                .filterNot { chapter -> chapter.id in downloadedChapterIds }
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
        onComplete: (Float) -> Unit,
    ) {
        scope.launch {
            var dbProgress = 0f
            try {
                val status = bibleVersionDao.getVersionById(versionId)?.status
                if (status != DownloadStatus.IN_PROGRESS) return@launch
                val chapterDto = json.decodeFromString<SyncChapterDto>(jsonString)
                val existingVerses = verseDao.getVersesByChapterId(chapterId).associateBy { it.number }
                val verseTextEntities = chapterDto.verses.mapNotNull { verseDto ->
                    existingVerses[verseDto.number]?.let { verseEntity ->
                        VerseTextEntity(
                            verseId = verseEntity.id,
                            bibleVersionId = versionId,
                            text = verseDto.text,
                            heading = verseDto.heading,
                        )
                    }
                }
                verseDao.upsertVerseTexts(verseTextEntities)
                val totalChapters = bibleVersionDao.getVersionById(versionId)?.totalChapters ?: 0
                val dbDownloaded = countDownloadedChapters(
                    versionId = versionId,
                    totalChapters = totalChapters,
                )
                dbProgress = if (totalChapters > 0) dbDownloaded.toFloat() / totalChapters else 0f
                Logger.d("PROGRESS") { "DB progress for $versionId: $dbDownloaded/$totalChapters = $dbProgress" }
            } catch (e: Exception) {
                Logger.e(e) { "Error processing downloaded chapter $chapterId for $versionId" }
            } finally {
                onComplete(dbProgress)
            }
        }
    }

    fun finalizeVersionIfComplete(
        versionId: String,
        onComplete: () -> Unit,
    ) {
        scope.launch {
            try {
                val entity = bibleVersionDao.getVersionById(versionId) ?: return@launch
                if (entity.status == DownloadStatus.DONE) return@launch
                var downloaded = verseDao.countChaptersWithVersesByVersion(versionId)
                // Guard against a SQLite WAL read-after-write race: all Swift onComplete()
                // callbacks have fired (meaning all DB writes completed), but the count query
                // may briefly observe a stale snapshot. If we're within 1 chapter of the total,
                // retry once after a short delay before deciding the version isn't fully done.
                if (downloaded == entity.totalChapters - 1) {
                    delay(300.milliseconds)
                    downloaded = verseDao.countChaptersWithVersesByVersion(versionId)
                }
                val name = resolveVersionName(versionId)
                if (downloaded >= entity.totalChapters) {
                    val remoteContentVersion = getRemoteContentVersion(versionId)
                    if (remoteContentVersion.isNotEmpty()) {
                        bibleVersionDao.updateContentVersion(
                            id = versionId,
                            contentVersion = remoteContentVersion,
                        )
                    }
                    bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE)
                    notifier.showComplete(versionId, name)
                } else {
                    // Some tasks failed permanently after exhausting retries on the iOS side.
                    // Move to PAUSED so the user can retry — getPendingDownloads will only
                    // re-fetch the missing chapters on the next attempt.
                    bibleVersionDao.updateStatus(versionId, DownloadStatus.PAUSED)
                    notifier.showError(versionId, name)
                }
            } catch (e: Exception) {
                Logger.e(e) { "Error finalizing version $versionId" }
            } finally {
                forgetDownloadedChapters(versionId)
                onComplete()
            }
        }
    }

    private suspend fun countDownloadedChapters(
        versionId: String,
        totalChapters: Int,
    ): Int = downloadedChaptersMutex.withLock {
        val counted = downloadedChapters[versionId]
            ?.let { previous -> previous + 1 }
            ?: verseDao.countChaptersWithVersesByVersion(versionId)
        val clamped = if (totalChapters > 0) counted.coerceAtMost(totalChapters) else counted
        downloadedChapters[versionId] = clamped
        clamped
    }

    private suspend fun forgetDownloadedChapters(versionId: String) = downloadedChaptersMutex.withLock {
        downloadedChapters.remove(versionId)
        Unit
    }

    private suspend fun resolveVersionName(versionId: String): String = bibleRepository
        .getBiblesFlow()
        .first()
        .find { it.version.id == versionId }
        ?.version
        ?.name ?: versionId
}
