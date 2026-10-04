package com.quare.bibleplanner.core.chapterlistening.domain.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ListeningSegmentModel(
    val chapter: ChapterLocationModel,
    val startVerse: Int?,
    val endVerse: Int?,
)
