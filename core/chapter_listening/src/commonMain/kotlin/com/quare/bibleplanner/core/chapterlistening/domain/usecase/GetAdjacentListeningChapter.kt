package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

/*
 * Why: implemented by the reader, which owns the reading order its own chapter arrows follow, so the
 * player never moves to a different chapter than the arrow next to it would.
 */
fun interface GetAdjacentListeningChapter {
    suspend operator fun invoke(
        chapter: ChapterLocationModel,
        direction: ChapterDirectionModel,
        shouldForceCanonOrder: Boolean,
    ): ChapterLocationModel?
}
