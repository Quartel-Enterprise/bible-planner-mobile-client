package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import kotlinx.coroutines.flow.Flow

interface VerseHighlightRepository {
    fun observeChapterHighlights(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<Map<Int, HighlightColor>>

    suspend fun getColors(refs: List<VerseRef>): Map<VerseRef, HighlightColor?>

    /** A null [color] clears the highlight, which travels as a tombstone rather than a delete. */
    suspend fun setColor(
        refs: List<VerseRef>,
        color: HighlightColor?,
    )

    suspend fun removeAllWithColor(colorKey: String)
}
