package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.ChapterRef
import org.jetbrains.compose.resources.StringResource

data class ReadChapterUiModel(
    val chapter: ChapterRef,
    val bookStringResource: StringResource,
    val isRead: Boolean,
    val verses: List<VerseUiModel>,
)
