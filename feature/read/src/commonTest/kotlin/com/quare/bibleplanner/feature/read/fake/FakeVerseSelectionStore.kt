package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stands in for the shared selection store. The real toggle semantics — ordering, deselecting the
 * last verse, switching chapter or version — are covered by the store's own test; this only needs to
 * behave consistently enough for the reader to react to.
 */
internal class FakeVerseSelectionStore {
    private val _selection = MutableStateFlow<VerseSelection?>(null)
    val selection: StateFlow<VerseSelection?> = _selection

    fun toggle(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection? {
        val previousNumbers = _selection.value
            ?.takeIf { it.chapter == chapter }
            ?.verseNumbers
            .orEmpty()
        val verseNumbers = if (verseNumber in previousNumbers) {
            previousNumbers - verseNumber
        } else {
            (previousNumbers + verseNumber).sorted()
        }
        _selection.value = verseNumbers
            .takeIf { it.isNotEmpty() }
            ?.let {
                VerseSelection(
                    chapter = chapter,
                    verseNumbers = it,
                )
            }
        return _selection.value
    }

    fun clear() {
        _selection.value = null
    }
}
