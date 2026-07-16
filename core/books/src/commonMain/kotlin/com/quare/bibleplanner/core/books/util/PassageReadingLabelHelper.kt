package com.quare.bibleplanner.core.books.util

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.plan.PassageModel
import org.jetbrains.compose.resources.getString

/**
 * Human passage label for a day, e.g. "Gênesis 4–7" or "Salmos 32, 122". Uses the localized book
 * name plus the preformatted [PassageModel.chapterRanges]; multiple passages join with ", ".
 */
@Composable
fun List<PassageModel>.toReadingLabel(): String =
    map { passage -> passage.toLabel(passage.bookId.getBookName()) }.joinToString(separator = ", ")

/**
 * Suspend variant of [toReadingLabel] for use outside composition (e.g. a ViewModel), resolving
 * book names via the suspend [getString] instead of stringResource.
 */
suspend fun List<PassageModel>.getReadingLabel(): String =
    map { passage -> passage.toLabel(getString(passage.bookId.toBookNameResource())) }
        .joinToString(separator = ", ")

private fun PassageModel.toLabel(bookName: String): String =
    if (chapterRanges.isNullOrEmpty()) bookName else "$bookName $chapterRanges"
