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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.BibleVersionChip
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadStatusPill
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadErrorContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadLoadingContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.chapterContent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

private val readingColumnMaxWidth = 640.dp
private val titleMinColumnWidth = 520.dp

/**
 * On a wide window the selection tools move into a panel beside the text instead of covering it, so
 * the verses stay visible while the user works on them.
 */
@Composable
internal fun ReadWideScreen(
    platform: Platform,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ReadWideHeader(
                platform = platform,
                header = state.header,
                onEvent = onEvent,
            )
            Box(
                modifier = Modifier.fillMaxSize(),
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
                                .widthIn(max = readingColumnMaxWidth)
                                .fillMaxHeight()
                                .padding(horizontal = 24.dp),
                            state = rememberLazyListState(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            content.chapters.forEachIndexed { index, chapter ->
                                chapterContent(
                                    chapter = chapter,
                                    header = state.header,
                                    settings = state.settings,
                                    isPrimaryChapter = index == 0,
                                    focusedVerseNumber = null,
                                    onEvent = onEvent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadWideHeader(
    platform: Platform,
    header: ReadHeaderUiModel,
    onEvent: (ReadUiEvent) -> Unit,
) = BoxWithConstraints {
    val hasRoomForTitle = maxWidth >= titleMinColumnWidth
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
                text = "${stringResource(header.bookStringResource)} ${header.chapterNumber}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        ReadStatusPill(
            isRead = header.isChapterRead,
            isCompact = true,
            onClick = {
                onEvent(
                    ReadUiEvent.ToggleReadStatus(
                        bookId = header.bookId,
                        chapterNumber = header.chapterNumber,
                    ),
                )
            },
        )
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
