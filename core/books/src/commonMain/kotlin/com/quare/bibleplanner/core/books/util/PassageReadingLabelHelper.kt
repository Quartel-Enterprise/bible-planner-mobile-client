package com.quare.bibleplanner.core.books.util

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.plan.PassageModel
import org.jetbrains.compose.resources.getString

@Composable
fun List<PassageModel>.toReadingLabel(): String =
    map { passage -> passage.toLabel(passage.bookId.getBookName()) }.joinToString(separator = ", ")

suspend fun List<PassageModel>.getReadingLabel(): String =
    map { passage -> passage.toLabel(getString(passage.bookId.toBookNameResource())) }
        .joinToString(separator = ", ")

private fun PassageModel.toLabel(bookName: String): String =
    if (chapterRanges.isNullOrEmpty()) bookName else "$bookName $chapterRanges"
