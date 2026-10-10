package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity

private const val OTHER_BOOK_CHAPTER_ID_OFFSET = 1_000L

internal fun chaptersOfOtherBooks(vararg bookIds: BookId): List<ChapterEntity> = BookId.entries
    .filterNot { it in bookIds }
    .map { bookId ->
        ChapterEntity(
            id = OTHER_BOOK_CHAPTER_ID_OFFSET + bookId.ordinal,
            number = 1,
            bookId = bookId.name,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        )
    }
