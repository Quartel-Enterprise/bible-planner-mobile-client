package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.passages_completed
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DayProgress(
    passages: List<PassagePlanModel>,
    books: List<BookDataModel>,
    modifier: Modifier = Modifier,
) {
    val (completedCount, totalCount) = calculateChapterCounts(
        passages = passages,
        books = books,
    )

    Text(
        text = stringResource(
            Res.string.passages_completed,
            completedCount,
            totalCount,
        ),
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

/**
 * Calculate the total number of chapters/items displayed and how many are completed.
 * Returns a Pair of (completedCount, totalCount).
 */
private fun calculateChapterCounts(
    passages: List<PassagePlanModel>,
    books: List<BookDataModel>,
): Pair<Int, Int> {
    var totalCount = 0
    var completedCount = 0

    passages.forEach { passage ->
        if (passage.chapters.isEmpty()) {
            // If no chapters specified, count as 1 item (the whole book)
            totalCount++
            if (passage.isRead) {
                completedCount++
            }
        } else {
            // Count each chapter as a separate item
            passage.chapters.forEach { chapter ->
                totalCount++
                val isChapterRead = isChapterReadForCount(
                    passage = passage,
                    chapter = chapter,
                    books = books,
                )
                if (isChapterRead) {
                    completedCount++
                }
            }
        }
    }

    return Pair(completedCount, totalCount)
}

/**
 * Check if a specific chapter within a passage is read by checking the book data.
 */
private fun isChapterReadForCount(
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

