package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel

internal class ChapterVersesUiModelMapper {
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
                    noteId = annotations.noteIdByVerse[number],
                )
            }
    }
}
