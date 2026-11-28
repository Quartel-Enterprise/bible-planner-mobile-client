package com.quare.bibleplanner.android.feature.day

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

// Why: the baseline profile journeys (tools/baseline-profile Journeys.kt) find the chapters by this tag.
private const val DAY_PASSAGES_TAG = "day_passages"

@Composable
internal fun DayPassagesCard(
    passages: List<PassageModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = passages.toRows(chapterReadStatus)
    ElevatedCard(
        modifier = modifier
            .testTag(DAY_PASSAGES_TAG)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index, row ->
                PassageRow(
                    row = row,
                    onEvent = onEvent,
                )
                if (index < rows.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

// Why: a passage without chapters is a whole book, read and toggled as one row.
private fun List<PassageModel>.toRows(chapterReadStatus: Map<Pair<Int, Int>, Boolean>): List<PassageRowModel> =
    flatMapIndexed { passageIndex, passage ->
        if (passage.chapters.isEmpty()) {
            listOf(
                PassageRowModel(
                    passage = passage,
                    chapter = null,
                    isRead = passage.isRead,
                    clickStrategy = ChapterClickStrategy.NavigateToFirstChapterOfTheBook(
                        bookId = passage.bookId,
                        isChapterRead = passage.isRead,
                    ),
                    toggleStrategy = UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex),
                ),
            )
        } else {
            passage.chapters.mapIndexed { chapterIndex, chapter ->
                PassageRowModel(
                    passage = passage,
                    chapter = chapter,
                    isRead = chapterReadStatus[passageIndex to chapterIndex] == true,
                    clickStrategy = ChapterClickStrategy.NavigateToChapter(
                        bookId = passage.bookId,
                        isChapterRead = passage.isRead,
                        chapterNumber = chapter.number,
                    ),
                    toggleStrategy = UpdateReadStatusOfPassageStrategy.Chapter(
                        passageIndex = passageIndex,
                        chapterIndex = chapterIndex,
                    ),
                )
            }
        }
    }

@Composable
private fun PassageRow(
    row: PassageRowModel,
    onEvent: (DayUiEvent) -> Unit,
) {
    val bookName = row.passage.bookId.getBookName()
    val chapter = row.chapter
    val startVerse = chapter?.startVerse
    val endVerse = chapter?.endVerse
    ListItem(
        modifier = Modifier.clickable { onEvent(DayUiEvent.OnChapterClick(row.clickStrategy)) },
        headlineContent = {
            Text(text = if (chapter == null) bookName else "$bookName ${chapter.number}")
        },
        supportingContent = if (startVerse != null && endVerse != null) {
            { Text(text = "${stringResource(R.string.day_verses)} $startVerse-$endVerse") }
        } else {
            null
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = row.isRead,
                    onCheckedChange = { onEvent(DayUiEvent.OnChapterCheckboxClick(row.toggleStrategy)) },
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
