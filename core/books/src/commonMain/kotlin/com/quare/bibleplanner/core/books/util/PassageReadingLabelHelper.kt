package com.quare.bibleplanner.core.books.util

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.plan.PassageModel

/**
 * Human passage label for a day, e.g. "Gênesis 4–7" or "Salmos 32, 122". Uses the localized book
 * name plus the preformatted [PassageModel.chapterRanges]; multiple passages join with ", ".
 */
@Composable
fun List<PassageModel>.toReadingLabel(): String {
    val labels = map { passage ->
        val bookName = passage.bookId.getBookName()
        if (passage.chapterRanges.isNullOrEmpty()) {
            bookName
        } else {
            "$bookName ${passage.chapterRanges}"
        }
    }
    return labels.joinToString(separator = ", ")
}
