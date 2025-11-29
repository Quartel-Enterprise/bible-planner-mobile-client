package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel

internal fun LazyListScope.passageList(
    passages: List<PassagePlanModel>,
    books: List<BookDataModel>,
    onChapterToggle: (passageIndex: Int, chapterIndex: Int) -> Unit,
) {
    itemsIndexed(passages) { passageIndex, passage ->
        if (passage.chapters.isEmpty()) {
            // If no chapters specified, show the whole book as a single item
            ChapterItem(
                bookName = passage.bookId.getBookName(),
                chapterNumber = null,
                isRead = passage.isRead,
                onToggle = { onChapterToggle(passageIndex, -1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        } else {
            // Show each chapter as a separate item
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val isChapterRead = isChapterRead(
                    passage = passage,
                    chapter = chapter,
                    books = books,
                )
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
        Checkbox(
            checked = isRead,
            onCheckedChange = { onToggle() },
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

/**
 * Check if a specific chapter within a passage is read by checking the book data.
 */
private fun isChapterRead(
    passage: PassagePlanModel,
    chapter: ChapterPlanModel,
    books: List<BookDataModel>,
): Boolean {
    val book = books.find { it.id == passage.bookId } ?: return false
    val bookChapter = book.chapters.find { it.number == chapter.number } ?: return false

    val startVerse = chapter.startVerse
    val endVerse = chapter.endVerse

    return when {
        // If verse range is specified, check those specific verses
        startVerse != null && endVerse != null -> {
            val requiredVerses = startVerse..endVerse
            requiredVerses.all { verseNumber ->
                bookChapter.verses.find { it.number == verseNumber }?.isRead == true
            }
        }

        // If only start verse is specified, check from that verse to end of chapter
        startVerse != null -> {
            bookChapter.verses
                .filter { it.number >= startVerse }
                .all { it.isRead }
        }

        // If no verse range specified, check if entire chapter is read
        else -> {
            bookChapter.isRead
        }
    }
}
