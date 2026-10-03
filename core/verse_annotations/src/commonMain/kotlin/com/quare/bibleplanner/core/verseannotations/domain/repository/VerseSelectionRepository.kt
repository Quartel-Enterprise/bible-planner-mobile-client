package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlinx.coroutines.flow.StateFlow

interface VerseSelectionRepository {
    val selection: StateFlow<VerseSelection?>

    fun toggle(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection?

    fun clear()
}
