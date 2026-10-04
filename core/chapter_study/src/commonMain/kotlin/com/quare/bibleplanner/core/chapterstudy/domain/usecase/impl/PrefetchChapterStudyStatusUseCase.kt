package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.PrefetchChapterStudyStatus
import com.quare.bibleplanner.core.utils.suspendRunCatching

// Why: best-effort prefetch nobody waits on; a failure is swallowed and the tap fetches the status itself.
internal class PrefetchChapterStudyStatusUseCase(
    private val localAccessChecker: ChapterStudyLocalAccessChecker,
    private val quotaChecker: ChapterStudyQuotaChecker,
) : PrefetchChapterStudyStatus {
    override suspend fun invoke(target: ChapterStudyTargetModel) {
        suspendRunCatching {
            val check = localAccessChecker.check(target) as? ChapterStudyAccessCheck.NeedsStatus
                ?: return@suspendRunCatching
            val isAlreadyOpen = quotaChecker.isOpenedByPrefetchedStatus(
                key = check.key,
                target = target,
            )
            if (!isAlreadyOpen) quotaChecker.fetchAndKeepStatus(check.key)
        }.onFailure { throwable ->
            Logger.d(throwable) { "Could not prefetch the chapter study status" }
        }
    }
}
