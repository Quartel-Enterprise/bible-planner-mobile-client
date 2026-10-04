package com.quare.bibleplanner.core.verseannotations.domain.model

/*
 * Why: two notes can share a verse (a note added on a sub-passage, or two devices syncing), and
 * noteIdByVerse keeps only one of them per verse, so a note's passage is kept whole on its own.
 */
data class ChapterAnnotations(
    val highlightColorByVerse: Map<Int, HighlightColor>,
    val savedVerseNumbers: Set<Int>,
    val noteIdByVerse: Map<Int, String>,
    val noteVerseNumbersById: Map<String, List<Int>>,
)
