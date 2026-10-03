package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

@Composable
internal fun VisibleChapterEffect(
    visibleChapter: ReadChapterUiModel?,
    header: ReadHeaderUiModel,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val bookId = visibleChapter?.chapter?.bookId ?: header.bookId
    val chapterNumber = visibleChapter?.chapter?.chapterNumber ?: header.chapterNumber
    LaunchedEffect(bookId, chapterNumber) {
        onEvent(
            ReadUiEvent.OnVisibleChapterChanged(
                bookId = bookId,
                chapterNumber = chapterNumber,
            ),
        )
    }
}
