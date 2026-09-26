package com.quare.bibleplanner.core.books.fake

internal data class VerseRangeUpdate(
    val chapterId: Long,
    val startVerse: Int,
    val endVerse: Int,
    val isRead: Boolean,
    val updatedAt: Long,
)
