package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkPosition
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel

internal class ChapterVersesUiModelMapper {
    // Why: the index has a row for every verse any version uses (ESV and NIV lack Matthew 17:21), so
    // a missing text is skipped; an empty result means the chapter still has to be downloaded.
    fun map(
        versesWithTexts: List<VerseWithTexts>,
        versionId: String,
        annotations: ChapterAnnotations,
    ): List<VerseUiModel> = versesWithTexts.mapNotNull { verseWithTexts ->
        val number = verseWithTexts.verse.number
        verseWithTexts.texts
            .find { it.bibleVersionId == versionId }
            ?.let { verseText ->
                VerseUiModel(
                    number = number,
                    heading = verseText.heading,
                    text = verseText.text,
                    isSelected = false,
                    highlightColor = annotations.highlightColorByVerse[number],
                    isSaved = number in annotations.savedVerseNumbers,
                    noteMark = annotations.noteIdByVerse[number]?.let { noteId ->
                        toNoteMark(
                            noteId = noteId,
                            verseNumber = number,
                            noteVerseNumbers = annotations.noteVerseNumbersById.getValue(noteId),
                        )
                    },
                )
            }
    }

    // Why: a note can span verses that are not adjacent (1-3, 5), so the position follows the note's
    // own verses and the mark simply breaks over the verse left out.
    private fun toNoteMark(
        noteId: String,
        verseNumber: Int,
        noteVerseNumbers: List<Int>,
    ): VerseNoteMarkUiModel = VerseNoteMarkUiModel(
        noteId = noteId,
        noteVerseNumbers = noteVerseNumbers,
        position = when {
            noteVerseNumbers.size == 1 -> VerseNoteMarkPosition.SINGLE
            verseNumber == noteVerseNumbers.first() -> VerseNoteMarkPosition.FIRST
            verseNumber == noteVerseNumbers.last() -> VerseNoteMarkPosition.LAST
            else -> VerseNoteMarkPosition.MIDDLE
        },
    )
}
