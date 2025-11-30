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
    itemsIndexed(passages) { passageIndex, passage ->
        if (passage.chapters.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(maxContentWidth)) {
                    ChapterItem(
                        bookName = passage.bookId.getBookName(),
                        chapterNumber = null,
                        isRead = passage.isRead,
                        onToggle = { onChapterToggle(passageIndex, -1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                }
            }
            // Add divider after passage if it's not the last one
            if (passageIndex < passages.size - 1) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val isChapterRead = chapterReadStatus[passageIndex to chapterIndex] ?: false
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        ChapterItem(
                            bookName = passage.bookId.getBookName(),
                            chapterNumber = chapter.number,
                            isRead = isChapterRead,
                            onToggle = { onChapterToggle(passageIndex, chapterIndex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        )
                    }
                }
                // Add divider after each chapter except the last chapter of the last passage
                val isLastChapter = chapterIndex == passage.chapters.size - 1
                val isLastPassage = passageIndex == passages.size - 1
                if (!(isLastChapter && isLastPassage)) {
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
        modifier = modifier.clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isRead,
            onCheckedChange = { onToggle() }, // Handled by row click
        )
        Text(
            text = formatChapterText(bookName, chapterNumber),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
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
