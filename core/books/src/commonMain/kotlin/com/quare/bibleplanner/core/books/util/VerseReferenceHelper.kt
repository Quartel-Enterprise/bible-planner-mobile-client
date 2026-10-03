package com.quare.bibleplanner.core.books.util

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.book.BookId
import org.jetbrains.compose.resources.getString

fun List<Int>.toVerseNumbersLabel(): String = sorted()
    .distinct()
    .fold(mutableListOf<MutableList<Int>>()) { runs, verseNumber ->
        val lastRun = runs.lastOrNull()
        if (lastRun != null && lastRun.last() + 1 == verseNumber) {
            lastRun.add(verseNumber)
        } else {
            runs.add(mutableListOf(verseNumber))
        }
        runs
    }.joinToString(separator = ", ") { run ->
        if (run.size == 1) run.first().toString() else "${run.first()}-${run.last()}"
    }

@Composable
fun verseReferenceLabel(
    bookId: BookId,
    chapterNumber: Int,
    verseNumbers: List<Int>,
): String = buildVerseReferenceLabel(
    bookName = bookId.getBookName(),
    chapterNumber = chapterNumber,
    verseNumbers = verseNumbers,
)

suspend fun getVerseReferenceLabel(
    bookId: BookId,
    chapterNumber: Int,
    verseNumbers: List<Int>,
): String = buildVerseReferenceLabel(
    bookName = getString(bookId.toBookNameResource()),
    chapterNumber = chapterNumber,
    verseNumbers = verseNumbers,
)

private fun buildVerseReferenceLabel(
    bookName: String,
    chapterNumber: Int,
    verseNumbers: List<Int>,
): String {
    val chapterReference = "$bookName $chapterNumber"
    return if (verseNumbers.isEmpty()) {
        chapterReference
    } else {
        "$chapterReference:${verseNumbers.toVerseNumbersLabel()}"
    }
}
