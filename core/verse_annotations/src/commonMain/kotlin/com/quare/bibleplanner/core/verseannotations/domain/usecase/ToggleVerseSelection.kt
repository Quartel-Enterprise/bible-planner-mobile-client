package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection

fun interface ToggleVerseSelection {
    /** @return the selection after the toggle, or null once the last verse is deselected. */
    operator fun invoke(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection?
}
