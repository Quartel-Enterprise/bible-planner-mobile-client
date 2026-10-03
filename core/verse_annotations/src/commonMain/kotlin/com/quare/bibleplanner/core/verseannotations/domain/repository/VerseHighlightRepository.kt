package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseHighlight
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import kotlinx.coroutines.flow.Flow

interface VerseHighlightRepository {
    fun observeChapterHighlights(chapter: ChapterRef): Flow<Map<Int, HighlightColor>>

    fun observeVersionHighlights(bibleVersionId: String): Flow<List<VerseHighlight>>

    suspend fun getColors(refs: List<VerseRef>): Map<VerseRef, HighlightColor?>

    // Why: a null color clears the highlight as a tombstone, not a delete, so it syncs.
    suspend fun setColor(
        refs: List<VerseRef>,
        color: HighlightColor?,
    )

    suspend fun removeAllWithColor(colorKey: String)
}
