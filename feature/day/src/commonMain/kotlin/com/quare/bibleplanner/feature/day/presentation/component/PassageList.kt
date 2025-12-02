package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassagePlanModel

internal fun LazyListScope.passageList(
    passages: List<PassagePlanModel>,
    chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
    onChapterToggle: (passageIndex: Int, chapterIndex: Int) -> Unit,
    maxContentWidth: Dp,
) {
    passages.forEachIndexed { passageIndex, passage ->
        val onToggle = { onChapterToggle(passageIndex, -1) }
        if (passage.chapters.isEmpty()) {
            centeredClickableItem(maxContentWidth, onToggle) {
                ChapterItem(
                    bookName = passage.bookId.getBookName(),
                    chapterNumber = null,
                    isRead = passage.isRead,
                    onToggle = onToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                        .padding(vertical = 8.dp),
                )
            }
            if (passageIndex < passages.size - 1) {
                dividerItem(maxContentWidth)
            }
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val chapterToggle = { onChapterToggle(passageIndex, chapterIndex) }
                val isChapterRead = chapterReadStatus[passageIndex to chapterIndex] ?: false
                centeredClickableItem(maxContentWidth, chapterToggle) {
                    ChapterItem(
                        bookName = passage.bookId.getBookName(),
                        chapterNumber = chapter.number,
                        isRead = isChapterRead,
                        onToggle = chapterToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                            .padding(vertical = 8.dp),
                    )
                }
                val isLastChapter = chapterIndex == passage.chapters.size - 1
                val isLastPassage = passageIndex == passages.size - 1
                if (!(isLastChapter && isLastPassage)) {
                    dividerItem(maxContentWidth)
                }
            }
        }
    }
}

private fun LazyListScope.centeredClickableItem(
    maxContentWidth: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                content()
            }
        }
    }
}

private fun LazyListScope.dividerItem(maxContentWidth: Dp) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.width(maxContentWidth)) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun ChapterItem(
    bookName: String,
    chapterNumber: Int?,
    isRead: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatChapterText(bookName, chapterNumber),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = isRead,
            onCheckedChange = { onToggle() }, // Handled by row click
        )
    }
}

private fun formatChapterText(
    bookName: String,
    chapterNumber: Int?,
): String = if (chapterNumber == null) {
    bookName
} else {
    "$bookName $chapterNumber"
}
