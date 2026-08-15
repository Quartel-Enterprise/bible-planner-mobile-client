package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import org.jetbrains.compose.resources.StringResource

/**
 * One chapter laid out for reading. The reader shows a single chapter, except in vertical reading,
 * where the next one is appended to the same scroll so the text keeps going.
 */
data class ReadChapterUiModel(
    val bookId: BookId,
    val bookStringResource: StringResource,
    val chapterNumber: Int,
    val isRead: Boolean,
    val verses: List<VerseUiModel>,
)
