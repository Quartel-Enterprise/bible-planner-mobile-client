package com.quare.bibleplanner.core.model.book

data class BookChapterModel(
    val number: Int,
    val verses: List<VerseModel>,
    val isRead: Boolean,
    val readUpdatedAt: Long?,
)
