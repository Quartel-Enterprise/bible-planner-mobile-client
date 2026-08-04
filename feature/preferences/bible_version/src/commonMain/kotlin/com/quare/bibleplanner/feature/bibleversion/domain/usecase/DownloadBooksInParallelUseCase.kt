package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class DownloadBooksInParallelUseCase(
    private val getPrioritizedBookIds: GetPrioritizedBookIdsUseCase,
    private val downloadChapters: DownloadChaptersUseCase,
) {
    suspend operator fun invoke(
        versionId: String,
        force: Boolean,
    ): Result<Unit> = suspendRunCatching {
        val results = supervisorScope {
            getPrioritizedBookIds()
                .map { bookId ->
                    async {
                        downloadChapters(
                            versionId = versionId,
                            bookId = bookId,
                            force = force,
                        )
                    }
                }.awaitAll()
        }
        results.firstOrNull { it.isFailure }?.getOrThrow()
    }
}
