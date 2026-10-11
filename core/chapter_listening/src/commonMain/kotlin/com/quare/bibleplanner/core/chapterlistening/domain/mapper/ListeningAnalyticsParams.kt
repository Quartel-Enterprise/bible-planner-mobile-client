package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams

fun ChapterLocationModel.toListeningAnalyticsParams(): Map<String, Any> = mapOf(
    AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
    AnalyticsParams.CHAPTER_NUMBER to chapterNumber,
)
