package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun chapterTitle(chapter: ChapterLocationModel): String =
    "${stringResource(chapter.bookId.toBookNameResource())} ${chapter.chapterNumber}"

@Composable
internal fun verseReference(
    chapter: ChapterLocationModel,
    verseNumber: Int?,
): String {
    val title = chapterTitle(chapter)
    return if (verseNumber == null) title else "$title:$verseNumber"
}
