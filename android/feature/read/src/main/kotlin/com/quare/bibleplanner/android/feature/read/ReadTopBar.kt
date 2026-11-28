package com.quare.bibleplanner.android.feature.read

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.utils.asStable

/*
 * Why: titles visibleChapter, not the opened one, because vertical reading scrolls
 * through chapters without leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadTopBar(
    header: ReadHeaderUiModel,
    visibleChapter: ReadChapterUiModel?,
    isTitleVisible: Boolean,
    isOpeningChapterStudy: Boolean,
    isChapterStudyBeside: Boolean,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookId = visibleChapter?.chapter?.bookId ?: header.bookId
    val chapterNumber = visibleChapter?.chapter?.chapterNumber ?: header.chapterNumber
    TopAppBar(
        modifier = modifier,
        scrollBehavior = topAppBarScrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets.asStable(),
        title = {
            AnimatedVisibility(
                visible = isTitleVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text = verseReferenceLabel(
                        bookId = bookId,
                        chapterNumber = chapterNumber,
                        verseNumbers = emptyList(),
                    ),
                )
            }
        },
        navigationIcon = {
            ReadBackButton(onClick = { onEvent(ReadUiEvent.OnArrowBackClick) })
        },
        actions = {
            ReaderAppearanceButton(onClick = { onEvent(ReadUiEvent.OnAppearanceClick) })
            if (!isChapterStudyBeside) {
                ChapterStudyPill(
                    isLoading = isOpeningChapterStudy,
                    onClick = {
                        onEvent(
                            ReadUiEvent.OnChapterStudyClick(
                                bookId = bookId,
                                chapterNumber = chapterNumber,
                                source = ChapterStudyEntrySource.TOP_BAR,
                            ),
                        )
                    },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
            BibleVersionChip(
                versionName = header.versionAbbreviation,
                onClick = { onEvent(ReadUiEvent.ManageBibleVersions) },
            )
        },
    )
}
