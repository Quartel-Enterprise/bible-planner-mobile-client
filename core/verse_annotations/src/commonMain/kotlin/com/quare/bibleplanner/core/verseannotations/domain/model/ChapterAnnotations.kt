package com.quare.bibleplanner.core.verseannotations.domain.model

data class ChapterAnnotations(
    val highlightColorByVerse: Map<Int, HighlightColor>,
    val savedVerseNumbers: Set<Int>,
    val noteIdByVerse: Map<Int, String>,
)
