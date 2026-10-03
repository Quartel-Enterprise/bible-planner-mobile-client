package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class DownloadBooksInParallelUseCase(
    private val getPrioritizedBookIds: GetPrioritizedBookIdsUseCase,
    private val downloadChapters: DownloadChaptersUseCase,
) {
    private val bookSemaphore = Semaphore(permits = MAX_CONCURRENT_BOOKS)

    suspend operator fun invoke(versionId: String): Result<Unit> = suspendRunCatching {
        val results = supervisorScope {
            getPrioritizedBookIds()
                .map { bookId -> async { bookSemaphore.withPermit { downloadChapters(versionId, bookId) } } }
                .awaitAll()
        }
        results.firstOrNull { it.isFailure }?.getOrThrow()
    }

    private companion object {
        const val MAX_CONCURRENT_BOOKS = 8
    }
}
