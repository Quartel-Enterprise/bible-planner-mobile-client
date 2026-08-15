package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import kotlinx.coroutines.flow.Flow

interface SavedVerseRepository {
    fun observeChapterSavedVerses(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<Set<Int>>

    suspend fun areAllSaved(refs: List<VerseRef>): Boolean

    suspend fun setSaved(
        refs: List<VerseRef>,
        isSaved: Boolean,
    )
}
