package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlinx.coroutines.flow.StateFlow

// Why: deliberately not persisted; a selection only means something while it is on screen.
interface VerseSelectionRepository {
    val selection: StateFlow<VerseSelection?>

    fun toggle(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection?

    fun clear()
}
