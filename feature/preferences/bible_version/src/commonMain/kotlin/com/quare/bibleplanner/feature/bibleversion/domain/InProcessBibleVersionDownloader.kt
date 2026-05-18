package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class InProcessBibleVersionDownloader(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val downloadBible: DownloadBibleUseCase,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activeDownloads = mutableMapOf<String, Job>()

    fun startDownload(versionId: String) {
        if (activeDownloads.containsKey(versionId)) return
        activeDownloads[versionId] = scope.launch {
            val versionName = resolveVersionName(versionId)
            bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.IN_PROGRESS)
            notifier.showProgress(versionId, versionName, 0f)

            val progressObserver = launch {
                combine(
                    bibleVersionDao.getAllVersionsFlow().mapNotNull { list -> list.find { it.id == versionId } },
                    verseDao.countChaptersWithVersesByVersionFlow(versionId),
                ) { entity, count -> count.toFloat() / entity.totalChapters }
                    .distinctUntilChanged()
                    .collect { progress -> notifier.showProgress(versionId, versionName, progress) }
            }

            val result = downloadBible(versionId)
            progressObserver.cancel()

            result
                .onSuccess { notifier.showComplete(versionId, versionName) }
                .onFailure {
                    notifier.showError(versionId, versionName)
                    bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
                }

            activeDownloads.remove(versionId)
        }
    }

    fun cancelDownload(versionId: String) {
        activeDownloads.remove(versionId)?.cancel()
    }

    fun cancelAllDownloads() {
        activeDownloads.keys.toList().forEach { activeDownloads.remove(it)?.cancel() }
    }

    fun hasActiveDownload(versionId: String): Boolean = activeDownloads.containsKey(versionId)

    fun hasAnyActiveDownload(): Boolean = activeDownloads.isNotEmpty()

    fun resumePendingDownloads(onComplete: (Boolean) -> Unit) {
        scope.launch {
            val pending = bibleVersionDao
                .getAllVersions()
                .filter { it.status == DownloadStatus.IN_PROGRESS }

            if (pending.isEmpty()) {
                onComplete(true)
                return@launch
            }

            pending
                .filter { !hasActiveDownload(it.id) }
                .forEach { version -> startDownload(version.id) }

            val jobs = pending.mapNotNull { version -> activeDownloads[version.id] }
            jobs.joinAll()
            onComplete(true)
        }
    }

    private suspend fun resolveVersionName(versionId: String): String = bibleRepository
        .getBiblesFlow()
        .first()
        .find { it.version.id == versionId }
        ?.version
        ?.name ?: versionId
}
