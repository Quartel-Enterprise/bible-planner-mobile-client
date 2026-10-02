package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.platform.Platform

internal data class ChapterStudyUiState(
    val bookId: BookId,
    val chapterNumber: Int,
    val platform: Platform,
    val content: ChapterStudyContentUiState,
)
