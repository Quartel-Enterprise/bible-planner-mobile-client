package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection

fun interface ToggleVerseSelection {
    operator fun invoke(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection?
}
