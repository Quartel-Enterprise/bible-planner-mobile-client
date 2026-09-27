package com.quare.bibleplanner.core.verseannotations.domain.factory

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.SavedVerse
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseHighlight
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseMark
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

internal class AnnotatedPassageFactory {
    fun create(
        highlights: List<VerseHighlight>,
        savedVerses: List<SavedVerse>,
        notes: List<VerseNote>,
    ): List<AnnotatedPassage> {
        val markByRef = createMarks(
            highlights = highlights,
            savedVerses = savedVerses,
        )
        val claimedRefs = mutableSetOf<VerseRef>()
        val notePassages = notes
            .sortedBy { it.createdAtEpochMillis }
            .map { note ->
                note.toPassage(
                    markByRef = markByRef,
                    claimedRefs = claimedRefs,
                )
            }
        val markPassages = markByRef
            .filterKeys { it !in claimedRefs }
            .toRunPassages()
        return (notePassages + markPassages).sortedWith(
            compareByDescending<AnnotatedPassage> { it.updatedAtEpochMillis }
                .thenBy { it.chapter.bookId.ordinal }
                .thenBy { it.chapter.chapterNumber }
                .thenBy { it.verseNumbers.first() },
        )
    }

    private fun createMarks(
        highlights: List<VerseHighlight>,
        savedVerses: List<SavedVerse>,
    ): Map<VerseRef, VerseMark> {
        val highlightByRef = highlights.associateBy { it.ref }
        val savedVerseByRef = savedVerses.associateBy { it.ref }
        return (highlightByRef.keys + savedVerseByRef.keys).associateWith { ref ->
            val highlight = highlightByRef[ref]
            val savedVerse = savedVerseByRef[ref]
            VerseMark(
                color = highlight?.color,
                isSaved = savedVerse != null,
                updatedAtEpochMillis = listOfNotNull(
                    highlight?.updatedAtEpochMillis,
                    savedVerse?.updatedAtEpochMillis,
                ).max(),
            )
        }
    }

    private fun VerseNote.toPassage(
        markByRef: Map<VerseRef, VerseMark>,
        claimedRefs: MutableSet<VerseRef>,
    ): AnnotatedPassage {
        val sortedVerseNumbers = verseNumbers.sorted().distinct()
        val refs = sortedVerseNumbers.map { verseNumber ->
            VerseRef(
                chapter = chapter,
                verseNumber = verseNumber,
            )
        }
        val marks = refs.map { markByRef[it] }
        val hasUniformMarks = marks.map { it?.toLookKey() }.distinct().size == 1
        val canClaim = hasUniformMarks && refs.none { it in claimedRefs }
        if (canClaim) claimedRefs.addAll(refs)
        val sharedMark = marks.firstOrNull()?.takeIf { canClaim }
        return AnnotatedPassage(
            chapter = chapter,
            verseNumbers = sortedVerseNumbers,
            highlightColor = sharedMark?.color,
            isSaved = sharedMark?.isSaved == true,
            note = this,
            updatedAtEpochMillis = if (canClaim) {
                (marks.mapNotNull { it?.updatedAtEpochMillis } + updatedAtEpochMillis).max()
            } else {
                updatedAtEpochMillis
            },
        )
    }

    private fun Map<VerseRef, VerseMark>.toRunPassages(): List<AnnotatedPassage> = entries
        .groupBy { (ref, _) -> ref.chapter }
        .flatMap { (chapter, chapterEntries) ->
            chapterEntries
                .sortedBy { (ref, _) -> ref.verseNumber }
                .fold(mutableListOf<MutableList<Map.Entry<VerseRef, VerseMark>>>()) { runs, entry ->
                    val previous = runs.lastOrNull()?.last()
                    val continuesRun = previous != null &&
                        previous.key.verseNumber + 1 == entry.key.verseNumber &&
                        previous.value.toLookKey() == entry.value.toLookKey()
                    if (continuesRun) runs.last().add(entry) else runs.add(mutableListOf(entry))
                    runs
                }.map { run ->
                    val mark = run.first().value
                    AnnotatedPassage(
                        chapter = chapter,
                        verseNumbers = run.map { (ref, _) -> ref.verseNumber },
                        highlightColor = mark.color,
                        isSaved = mark.isSaved,
                        note = null,
                        updatedAtEpochMillis = run.maxOf { (_, runMark) -> runMark.updatedAtEpochMillis },
                    )
                }
        }

    private fun VerseMark.toLookKey(): Pair<String?, Boolean> = color?.key to isSaved
}
