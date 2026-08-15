package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection

fun interface ToggleVerseSelection {
    /** @return the selection after the toggle, or null once the last verse is deselected. */
    operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        verseNumber: Int,
    ): VerseSelection?
}
