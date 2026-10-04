package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.utils.asStable
import org.jetbrains.compose.resources.stringResource

/*
 * Why: titles visibleChapter, not the opened one, because vertical reading scrolls
 * through chapters without leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadTopBar(
    platform: Platform,
    header: ReadHeaderUiModel,
    visibleChapter: ReadChapterUiModel?,
    isTitleVisible: Boolean,
    isOpeningChapterStudy: Boolean,
    isChapterStudyBeside: Boolean,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookName = stringResource(visibleChapter?.bookStringResource ?: header.bookStringResource)
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
                Text(text = "$bookName $chapterNumber")
            }
        },
        navigationIcon = {
            BackIcon(
                platform = platform,
                onBackClick = { onEvent(ReadUiEvent.OnArrowBackClick) },
            )
        },
        actions = {
            CommonIconButton(
                imageVector = Icons.Default.TextFormat,
                contentDescription = stringResource(Res.string.reader_appearance),
                onClick = { onEvent(ReadUiEvent.OnAppearanceClick) },
            )
            if (!isChapterStudyBeside) {
                ChapterStudyPill(
                    isLoading = isOpeningChapterStudy,
                    onClick = {
                        onEvent(
                            ReadUiEvent.OnChapterStudyClick(
                                bookId = visibleChapter?.chapter?.bookId ?: header.bookId,
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
