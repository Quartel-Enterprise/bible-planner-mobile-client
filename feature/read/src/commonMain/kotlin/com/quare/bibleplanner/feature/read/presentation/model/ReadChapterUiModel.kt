package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.ChapterRef
import org.jetbrains.compose.resources.StringResource

/**
 * One chapter laid out for reading. The reader shows a single chapter, except in vertical reading,
 * where the next one is appended to the same scroll so the text keeps going.
 *
 * It carries the version it was rendered in, because that is what a verse tapped here belongs to.
 */
data class ReadChapterUiModel(
    val chapter: ChapterRef,
    val bookStringResource: StringResource,
    val isRead: Boolean,
    val verses: List<VerseUiModel>,
)
