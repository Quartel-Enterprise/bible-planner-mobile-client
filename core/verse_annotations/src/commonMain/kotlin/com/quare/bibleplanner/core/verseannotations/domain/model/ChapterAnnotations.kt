package com.quare.bibleplanner.core.verseannotations.domain.model

/**
 * Everything the reader needs to decorate one chapter: the colour of each highlighted verse, the
 * verses the user bookmarked, and the id of the note covering each annotated verse. All three are
 * keyed by verse number so the reader can look them up while laying out the chapter.
 */
data class ChapterAnnotations(
    val highlightColorByVerse: Map<Int, HighlightColor>,
    val savedVerseNumbers: Set<Int>,
    val noteIdByVerse: Map<Int, String>,
)
