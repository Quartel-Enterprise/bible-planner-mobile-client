package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class VerseSelectionRepositoryImpl : VerseSelectionRepository {
    private val _selection = MutableStateFlow<VerseSelection?>(null)
    override val selection: StateFlow<VerseSelection?> = _selection

    override fun toggle(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection? {
        _selection.update { current ->
            val previousNumbers = current
                ?.verseNumbers
                .orEmpty()
                .takeIf { current?.chapter == chapter }
                .orEmpty()
            val verseNumbers = if (verseNumber in previousNumbers) {
                previousNumbers - verseNumber
            } else {
                (previousNumbers + verseNumber).sorted()
            }
            if (verseNumbers.isEmpty()) {
                null
            } else {
                VerseSelection(
                    chapter = chapter,
                    verseNumbers = verseNumbers,
                )
            }
        }
        return _selection.value
    }

    override fun clear() {
        _selection.update { null }
    }
}
