package com.quare.bibleplanner.feature.read.presentation.vertical

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetPreviousChapter
import kotlinx.coroutines.CoroutineScope

internal class ReadVerticalChaptersFactory(
    private val getNextChapter: GetNextChapter,
    private val getPreviousChapter: GetPreviousChapter,
) {
    fun create(
        scope: CoroutineScope,
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadVerticalChapters = ReadVerticalChapters(
        scope = scope,
        bookId = bookId,
        chapterNumber = chapterNumber,
        shouldForceCanonOrder = shouldForceCanonOrder,
        getNextChapter = getNextChapter,
        getPreviousChapter = getPreviousChapter,
    )
}
