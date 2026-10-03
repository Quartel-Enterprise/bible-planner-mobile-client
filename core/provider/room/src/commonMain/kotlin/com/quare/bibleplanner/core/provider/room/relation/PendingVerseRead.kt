package com.quare.bibleplanner.core.provider.room.relation

data class PendingVerseRead(
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val isRead: Boolean,
    val readUpdatedAt: Long?,
)
