package com.quare.bibleplanner.feature.day.domain.model

import com.quare.bibleplanner.core.model.book.BookId

sealed interface ChapterClickStrategy {

    val isChapterRead: Boolean
    val bookId: BookId

    data class NavigateToFirstChapterOfTheBook(
        override val bookId: BookId,
        override val isChapterRead: Boolean,
    ) : ChapterClickStrategy

    data class NavigateToChapter(
        override val bookId: BookId,
        override val isChapterRead: Boolean,
        val chapterNumber: Int,
    ) : ChapterClickStrategy
}
