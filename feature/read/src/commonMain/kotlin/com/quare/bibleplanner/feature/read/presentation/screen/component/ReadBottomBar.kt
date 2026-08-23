package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadBottomBar(
    header: ReadHeaderUiModel,
    scrollBehavior: BottomAppBarScrollBehavior,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.navigationBars,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChapterNavigationButton(
                suggestion = header.navigationSuggestions.previous,
                isNext = false,
                onClick = onEvent,
            )
            ReadStatusPill(
                modifier = Modifier.weight(1f),
                isRead = header.isChapterRead,
                onClick = {
                    onEvent(
                        ReadUiEvent.ToggleReadStatus(
                            bookId = header.bookId,
                            chapterNumber = header.chapterNumber,
                        ),
                    )
                },
            )
            ChapterNavigationButton(
                suggestion = header.navigationSuggestions.next,
                isNext = true,
                onClick = onEvent,
            )
        }
    }
}
