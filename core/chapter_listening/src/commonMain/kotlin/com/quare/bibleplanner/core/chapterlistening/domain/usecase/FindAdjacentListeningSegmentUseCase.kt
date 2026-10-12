package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class FindAdjacentListeningSegmentUseCase(
    private val getAdjacentListeningChapter: GetAdjacentListeningChapter,
) {
    suspend operator fun invoke(
        current: ListeningSessionModel,
        direction: ChapterDirectionModel,
    ): ListeningSegmentModel? {
        val day = current.day
        if (current.mode == ListeningModeModel.DAY_READING && day != null) {
            val index = day.segments.indexOf(current.segment)
            val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
            return day.segments.getOrNull(index + step)
        }
        return suspendRunCatching {
            getAdjacentListeningChapter(
                chapter = current.segment.chapter,
                direction = direction,
                shouldForceCanonOrder = current.shouldForceCanonOrder,
            )
        }.onFailure { throwable -> Logger.e(throwable) { "Could not find the $direction listening chapter" } }
            .getOrNull()
            ?.toWholeChapterSegment()
    }
}
