package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

internal fun ChapterLocationModel.toWholeChapterSegment(): ListeningSegmentModel = ListeningSegmentModel(
    chapter = this,
    startVerse = null,
    endVerse = null,
)
