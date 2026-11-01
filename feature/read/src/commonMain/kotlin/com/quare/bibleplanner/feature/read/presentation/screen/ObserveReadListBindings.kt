package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.read.presentation.component.rememberListeningFollow
import com.quare.bibleplanner.feature.read.presentation.component.rememberVerseFlash
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.content.CHAPTER_SHIMMER_ITEM_COUNT

@Composable
internal fun observeReadListBindings(
    state: ReadUiState,
    listening: ReadListeningUiState,
    listState: LazyListState,
    onEvent: (ReadUiEvent) -> Unit,
): ReadListBindings {
    val chapters = (state.content as? ReadContentUiState.Success)?.chapters.orEmpty()
    val leadingItemCount = if (state.isLoadingPreviousChapter) CHAPTER_SHIMMER_ITEM_COUNT else 0
    val verseFlash = rememberVerseFlash(
        focus = state.verseFocus,
        chapters = chapters,
        listState = listState,
        leadingItemCount = leadingItemCount,
        isChapterStudyBeside = state.isChapterStudyBeside,
        onShown = { onEvent(ReadUiEvent.OnVerseFocusShown) },
    )
    val visibleChapter = rememberVisibleChapter(
        chapters = chapters,
        listState = listState,
        leadingItemCount = leadingItemCount,
        isChapterStudyBeside = state.isChapterStudyBeside,
    )
    VisibleChapterEffect(
        visibleChapter = visibleChapter,
        header = state.header,
        onEvent = onEvent,
    )
    ReachedEndEffect(
        listState = listState,
        chapters = chapters,
        onReachedEnd = { onEvent(ReadUiEvent.OnReachedEnd) },
    )
    ReachedStartEffect(
        listState = listState,
        chapters = chapters,
        onReachedStart = { onEvent(ReadUiEvent.OnReachedStart) },
    )
    val listeningFollow = rememberListeningFollow(
        player = listening.player,
        chapters = chapters,
        listState = listState,
        leadingItemCount = leadingItemCount,
        isChapterStudyBeside = state.isChapterStudyBeside,
    )
    return ReadListBindings(
        verseFlash = verseFlash,
        visibleChapter = visibleChapter,
        listeningFollow = listeningFollow,
    )
}
