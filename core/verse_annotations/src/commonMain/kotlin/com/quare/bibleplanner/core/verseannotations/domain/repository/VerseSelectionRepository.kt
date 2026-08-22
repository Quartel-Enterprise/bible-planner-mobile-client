package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlinx.coroutines.flow.StateFlow

/**
 * The live selection, shared between the reader — which owns the tapping — and the panel that acts
 * on it. It is deliberately not persisted: a selection only means something while it is on screen.
 */
interface VerseSelectionRepository {
    val selection: StateFlow<VerseSelection?>

    /** @return the selection after the toggle, so the caller can report what the tap did. */
    fun toggle(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection?

    fun clear()
}
