package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.PassageModel

@Composable
internal fun List<PassageModel>.toReadingLabel(): String {
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
