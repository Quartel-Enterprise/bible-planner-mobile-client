package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.DayCompletionBannerSlot
import com.quare.bibleplanner.feature.read.presentation.component.rememberListeningFollow
import com.quare.bibleplanner.feature.read.presentation.component.rememberVerseFlash
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningEntrySource
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.BibleVersionChip
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterStudyPill
import com.quare.bibleplanner.feature.read.presentation.screen.component.ListenToggleButton
import com.quare.bibleplanner.feature.read.presentation.screen.component.ListeningMiniPlayerBar
import com.quare.bibleplanner.feature.read.presentation.screen.component.ListeningOverlay
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadStatusPill
import com.quare.bibleplanner.feature.read.presentation.screen.content.CHAPTER_SHIMMER_ITEM_COUNT
import com.quare.bibleplanner.feature.read.presentation.screen.content.ChapterShimmerPosition
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadErrorContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadLoadingContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.chapterContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.chapterShimmerContent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.utils.asStable
import org.jetbrains.compose.resources.stringResource

private val readingColumnMaxWidth = 640.dp
private val titleMinColumnWidth = 520.dp
private val bannerMaxWidth = 560.dp

@Composable
internal fun ReadWideScreen(
    platform: Platform,
    state: ReadUiState,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
    dayCompletionBanner: DayCompletionBannerSlot,
) {
    val listState = rememberLazyListState()
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.asStable()),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                ReadWideHeader(
                    platform = platform,
                    header = state.header,
                    visibleChapter = visibleChapter,
                    isOpeningChapterStudy = state.isOpeningChapterStudy,
                    isChapterStudyBeside = state.isChapterStudyBeside,
                    listening = listening,
                    onEvent = onEvent,
                    onListeningEvent = onListeningEvent,
                )
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    when (val content = state.content) {
                        ReadContentUiState.Loading -> {
                            ReadLoadingContent(
                                modifier = Modifier
                                    .widthIn(max = readingColumnMaxWidth)
                                    .fillMaxSize(),
                            )
                        }

                        is ReadContentUiState.Error -> {
                            ReadErrorContent(
                                modifier = Modifier.fillMaxSize(),
                                header = state.header,
                                content = content,
                                onEvent = onEvent,
                            )
                        }

                        is ReadContentUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier
                                    .testTag(READ_CHAPTERS_TAG)
                                    .widthIn(max = readingColumnMaxWidth)
                                    .fillMaxHeight()
                                    .padding(horizontal = 24.dp),
                                state = listState,
                                contentPadding = PaddingValues(bottom = 24.dp),
                            ) {
                                if (state.isLoadingPreviousChapter) {
                                    chapterShimmerContent(ChapterShimmerPosition.LEADING)
                                }
                                content.chapters.forEach { chapter ->
                                    chapterContent(
                                        chapter = chapter,
                                        header = state.header,
                                        settings = state.settings,
                                        isChapterStudyBeside = state.isChapterStudyBeside,
                                        // Why: the header's listen button is always there, in both reading modes.
                                        isListenShortcutShown = false,
                                        focusedVerseNumber = null,
                                        verseFlash = verseFlash,
                                        listening = listening,
                                        onEvent = onEvent,
                                        onListeningEvent = onListeningEvent,
                                    )
                                }
                                if (state.isLoadingNextChapter) {
                                    chapterShimmerContent(ChapterShimmerPosition.TRAILING)
                                }
                            }
                        }
                    }
                    ListeningOverlay(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = readingColumnMaxWidth),
                        listening = listening,
                        follow = listeningFollow,
                        onEvent = onEvent,
                        onListeningEvent = onListeningEvent,
                    )
                }
                listening.player?.let { player ->
                    ListeningMiniPlayerBar(
                        player = player,
                        onEvent = onListeningEvent,
                    )
                }
            }
        }
        state.dayCompletionBanner?.let { day ->
            dayCompletionBanner.Content(
                day = day,
                onDismissRequest = { onEvent(ReadUiEvent.OnDayCompletionBannerDismissed) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = bannerMaxWidth)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 20.dp,
                    ),
            )
        }
    }
}

@Composable
private fun ReadWideHeader(
    platform: Platform,
    header: ReadHeaderUiModel,
    visibleChapter: ReadChapterUiModel?,
    isOpeningChapterStudy: Boolean,
    isChapterStudyBeside: Boolean,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
) {
    BoxWithConstraints {
        val hasRoomForTitle = maxWidth >= titleMinColumnWidth
        val bookStringResource = visibleChapter?.bookStringResource ?: header.bookStringResource
        val bookId = visibleChapter?.chapter?.bookId ?: header.bookId
        val chapterNumber = visibleChapter?.chapter?.chapterNumber ?: header.chapterNumber
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackIcon(
                platform = platform,
                onBackClick = { onEvent(ReadUiEvent.OnArrowBackClick) },
            )
            if (hasRoomForTitle) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${stringResource(bookStringResource)} $chapterNumber",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (listening.isAvailable) {
                val chapter = ChapterLocationModel(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                )
                val player = listening.player?.takeIf { it.chapter == chapter }
                ListenToggleButton(
                    isActive = player != null,
                    isPlaying = player?.status == ListeningStatusModel.PLAYING,
                    size = 40.dp,
                    onClick = {
                        onListeningEvent(
                            ReadListeningUiEvent.OnListenClick(
                                chapter = chapter,
                                source = ListeningEntrySource.HEADER,
                            ),
                        )
                    },
                )
            }
            ReadStatusPill(
                isRead = visibleChapter?.isRead ?: header.isChapterRead,
                isCompact = true,
                onClick = {
                    onEvent(
                        ReadUiEvent.ToggleReadStatus(
                            bookId = bookId,
                            chapterNumber = chapterNumber,
                        ),
                    )
                },
            )
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
                )
            }
            BibleVersionChip(
                versionName = header.versionAbbreviation,
                onClick = { onEvent(ReadUiEvent.ManageBibleVersions) },
            )
            CommonIconButton(
                imageVector = Icons.Default.TextFormat,
                contentDescription = stringResource(Res.string.reader_appearance),
                onClick = { onEvent(ReadUiEvent.OnAppearanceClick) },
            )
        }
    }
}
