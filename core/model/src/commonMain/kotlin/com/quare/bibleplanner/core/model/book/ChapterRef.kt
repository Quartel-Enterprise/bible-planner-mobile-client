package com.quare.bibleplanner.core.model.book

/**
 * One chapter of one bible version — the scope everything the reader marks belongs to.
 *
 * The version is part of it rather than context around it: translations split and merge verses, so
 * the same coordinates do not point at the same words everywhere. Keeping the three together means
 * "is this the same chapter?" is one equality rather than a condition each caller spells out, and
 * the trio only comes apart where the database needs columns.
 */
data class ChapterRef(
    val bibleVersionId: String,
    val bookId: BookId,
    val chapterNumber: Int,
)
