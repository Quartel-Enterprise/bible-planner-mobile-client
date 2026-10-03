package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.ChapterRef
import org.jetbrains.compose.resources.StringResource

// Why: carries its own ChapterRef (with version) because vertical reading appends the next chapter
// and a tapped verse belongs to the version it was rendered in.
data class ReadChapterUiModel(
    val chapter: ChapterRef,
    val bookStringResource: StringResource,
    val isRead: Boolean,
    val verses: List<VerseUiModel>,
)
