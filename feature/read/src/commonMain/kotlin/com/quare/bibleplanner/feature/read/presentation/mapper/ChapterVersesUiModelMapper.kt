package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel

internal class ChapterVersesUiModelMapper {
    /**
     * The verses [versionId] has text for, in order and decorated with [annotations].
     *
     * The local index holds a row for every verse number any version uses, so a version that
     * leaves a verse out — ESV and NIV have no Matthew 17:21 — simply has no text for that row.
     * Such a verse is skipped instead of failing the whole chapter. An empty result therefore means
     * the version has no text for the chapter at all: it still has to be downloaded.
     */
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
